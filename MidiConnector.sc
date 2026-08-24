MidiConnector : AbstractConnector {
	classvar <accum;
	classvar <onConnectorRemove;

	*initClass {
		// input accumulation of input in a linear range in 'endless' mode
		// see ccAction in prInitMidiConnection
		accum = ();
	}

	*onConnectorRemove_ { |func|
		onConnectorRemove = onConnectorRemove.addFunc(func)
	}

	*new { |widget, name|
		if (widget.class === Symbol or: { widget.isString }) {
			widget = CVWidget.all[widget.asSymbol]
		};
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("A MidiConnector can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget).init(name);
	}

	init { |name|
		this.widget.numMidiConnectors = this.widget.numMidiConnectors + 1;
		name ?? {
			name = "MIDI Connection %".format(this.widget.numMidiConnectors).asSymbol;
		};
		this.initModels(this.widget.wmc, name);
		this.widget.wmc.midiConnectors.m.value_(
			this.widget.wmc.midiConnectors.m.value.add(this)
		).changedPerformKeys(this.widget.syncKeys);
	}

	initModels { |wmc, name|
		wmc.midiOptions ?? { wmc.midiOptions = () };
		wmc.midiOptions.m ?? {
			wmc.midiOptions.m = Ref(List[]);
		};
		wmc.midiOptions.m.value.add((
			midiMode: CVWidget.midiMode,
			midiZero: CVWidget.midiZero,
			ctrlButtonGroup: CVWidget.midiCtrlButtonGroup,
			midiResolution: CVWidget.resolution,
			snapDistance: CVWidget.snapDistance,
			// special case: a classvar getter/setter can only be defined as a literal
			midiInputMapping: CVWidget.inputMapping
		));

		wmc.midiConnections ?? { wmc.midiConnections = () };
		wmc.midiConnections.m ?? {
			wmc.midiConnections.m = Ref(List[]);
		};
		wmc.midiConnections.m.value.add(nil);

		wmc.midiDisplay ?? { wmc.midiDisplay = () };
		wmc.midiDisplay.m ?? {
			wmc.midiDisplay.m = Ref(List[]);
		};
		wmc.midiDisplay.m.value.add((
			src: 'source...',
			chan: "chan",
			ctrl: "ctrl",
			learn: "L",
			toolTip: "Click and move hardware slider/knob to connect to",
			slotToolTip: "CVWidgetKnob '%' holds a single slot - setting not available."
		));
		wmc.midiConnectorNames ?? { wmc.midiConnectorNames = () };
		wmc.midiConnectorNames.m ?? {
			wmc.midiConnectorNames.m = Ref(List[]);
		};
		wmc.midiConnectorNames.m.value.add(name);
		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitMidiConnectors,
			prInitMidiConnectorNames,
			prInitMidiOptions,
			prInitMidiConnection,
			prInitMidiDisplay
		].do { |method|
			this.perform(method, wmc, this.widget.cv)
		}
	}

	// private: default controllers
	prInitMidiConnectors { |mc, cv|
		mc.midiConnectors.c ?? {
			mc.midiConnectors.c = SimpleController(mc.midiConnectors.m);
		};
		mc.midiConnectors.c.put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	prInitMidiOptions { |mc, cv|
		mc.midiOptions.c ?? {
			mc.midiOptions.c = SimpleController(mc.midiOptions.m);
		};
		mc.midiOptions.c.put(\default, { |changer, what ... moreArgs|
			// var index = mc.midiConnectors.m.value.indexOf(this);
			// do something...
		})
	}

	prInitMidiConnection { |mc, cv|
		mc.midiConnections.c ?? {
			mc.midiConnections.c = SimpleController(mc.midiConnections.m);
		};
		mc.midiConnections.c.put(\default, { |changer, what ... moreArgs|
			// do something...
		})
	}

	prInitMidiDisplay { |mc, cv|
		mc.midiDisplay.c ?? {
			mc.midiDisplay.c = SimpleController(mc.midiDisplay.m);
		};
		mc.midiDisplay.c.put(\default, { |changer, what ... moreArgs|
			// "midiDisplay.c.triggered".postln;
			// 	var index = mc.midiConnectors.m.value.indexOf(this);
			// 	// "midiDisplay.c - changer.value: %, moreArgs: %".format(changer.value, index).postln;
			// 	// ...
		})
	}

	prInitMidiConnectorNames { |mc, cv|
		mc.midiConnectorNames.c ?? {
			mc.midiConnectorNames.c = SimpleController(mc.midiConnectorNames.m);
		};
		mc.midiConnectorNames.c.put(\default, { |changer, what ... moreArgs|
			// 	"midiConnectorNames.c triggered:\n\t%\n\t%\n\t%".format(changer.value, what, moreArgs).postln;
		})
	}

	index {
		^this.widget.midiConnectors.indexOf(this);
	}

	name {
		^this.widget.wmc.midiConnectorNames.m.value[this.index]
	}

	name_ { |name|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.midiConnectorNames.m.value[index] = name.asSymbol;
		mc.midiConnectorNames.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	setMidiOption { |option, value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.midiOptions.m.value[index][option] = value;
		mc.midiOptions.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getMidiOption { |option|
		var index = this.index;
		^this.widget.wmc.midiOptions.m.value[index][option]
	}

	setMidiInputMapping { |mapping, curve(0), env(Env([0, 1], [1]))|
		var index = this.index;
		var mc = this.widget.wmc;
		mapping = mapping.asSymbol;
		[\linlin, \linexp, \explin, \expexp, \lincurve, \linbicurve, \linenv].indexOf(mapping) ?? {
			"arg 'mapping' must be one of \\linlin, \\linexp, \\explin, \\expexp, \\lincurve, \\linbicurve or \\linenv".error;
			^this
		};
		// special care needs to be taken to NOT set CVWidget.inputMapping
		// not working, would set CVWidget.inputMapping too:
		// mc..midiOptions.m.value[index].midiInputMapping.mapping = mapping;
		mc.midiOptions.m.value[index].midiInputMapping_((mapping: mapping));
		case
		{ mapping === \lincurve or: { mapping === \linbicurve }} {
			mc.midiOptions.m.value[index].midiInputMapping.curve = curve;
		}
		{ mapping === \linenv } {
			mc.midiOptions.m.value[index].midiInputMapping.env = env;
		};
		mc.midiOptions.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	setMidiDisplay { |displayValueName, value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.midiDisplay.m.value[index][displayValueName] = value;
		mc.midiDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getMidiDisplay { |displayValueName|
		var index = this.index;
		^this.widget.wmc.midiDisplay.m.value[index][displayValueName]
	}

	getSlotToolTip {
		^this.getMidiDisplay(\slotToolTip)
	}

	setMIDIFuncEnabled { |boolEnabled|
		var index = this.index;
		var m = this.widget.wmc.midiConnections.m;
		if (m.value[index].isNil) {
			"connector at index % is currently not connected.".format(index).inform
		} {
			if (boolEnabled) { m.value[index].enable } { m.value[index].disable };
			m.changedPerformKeys(this.widget.syncKeys, index);
		}
	}

	getMIDIFuncEnabled {
		if (this.widget.wmc.midiConnections.m.value[this.index].notNil) {
			^this.widget.wmc.midiConnections.m.value[this.index].enabled
		} { ^true }
	}


	midiConnect { |num, chan, srcID, argTemplate, dispatcher|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.midiConnections.m.value[index] = this.prMIDIFunc(index, num, chan, srcID, argTemplate, dispatcher);
		mc.midiConnections.m.changedPerformKeys(this.widget.syncKeys, index);
		mc.midiDisplay.m.value[index].learn = "X";
		mc.midiDisplay.m.value[index].toolTip = "Click to disconnect";
		mc.midiConnections.m.value[index].srcID !? {
			mc.midiDisplay.m.value[index].src = mc.midiConnections.m.value[index].srcID
		};
		mc.midiConnections.m.value[index].chan !? {
			mc.midiDisplay.m.value[index].chan = mc.midiConnections.m.value[index].chan
		};
		mc.midiConnections.m.value[index].msgNum !? {
			mc.midiDisplay.m.value[index].ctrl = mc.midiConnections.m.value[index].msgNum
		};
		mc.midiConnections.m.value[index].argTemplate !? {
			mc.midiDisplay.m.value[index].template = mc.midiConnections.m.value[index].argTemplate.cs
		};
		mc.midiConnections.m.value[index].dispatcher !? {
			mc.midiDisplay.m.value[index].dispatcher = mc.midiConnections.m.value[index].dispatcher.cs
		};
		mc.midiDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? { this.midiDisconnect }
		})
	}

	midiDisconnect {
		var mc = this.widget.wmc;
		var index = this.index;
		mc.midiConnections.m.value[index].free;
		mc.midiConnections.m.value[index] = nil;
		mc.midiConnections.m.changedPerformKeys(this.widget.syncKeys, index);
		mc.midiDisplay.m.value[index].src = 'source...';
		mc.midiDisplay.m.value[index].chan = "chan";
		mc.midiDisplay.m.value[index].ctrl = "ctrl";
		mc.midiDisplay.m.value[index].template = nil;
		mc.midiDisplay.m.value[index].dispatcher = nil;
		mc.midiDisplay.m.value[index].toolTip = "Click and move hardware slider/knob to connect to";
		mc.midiDisplay.m.value[index].learn = "L";
		mc.midiDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
		CmdPeriod.remove({
			this.widget !? { this.midiDisconnect }
		})
	}

	prMIDIFunc { |index, cc, ch, src, t, d|
		var snapDistance, inputMapping, input;
		var cv = this.widget.cv, learn;
		var makeFunc = { |argSrc, argChan, argNum, argTempl, argDispatcher|
			if (this.widget.wmc.midiConnections.m.value[index].isNil or: {
				this.widget.wmc.midiConnections.m.value[index].func.isNil
			}) {
				this.widget.wmc.midiConnections.m.value[index] = MIDIFunc.cc(
					ccAction,
					ccNum: argNum !? { argNum.asInteger },
					chan: argChan !? { argChan.asInteger },
					srcID: argSrc !? { argSrc.asInteger },
					argTemplate: argTempl,
					dispatcher: argDispatcher
				)
			};
			this.widget.wmc.midiConnections.m.value[index]
		};

		var ccAction = { |val, num, chan, src|
			// MIDI learn
			// we must infer the connections parameters here
			inputMapping = this.getMidiInputMapping;
			this.getMidiMode.switch(
				//  0-127
				0, {
					input = val/127;
					snapDistance = this.getMidiSnapDistance;
					if ((snapDistance <= 0).or(
						input < (cv.input + (snapDistance)) and: {
							input > (cv.input - (snapDistance))
					})) {
						case
						{ inputMapping.mapping === \lincurve } {
							if (inputMapping.curve != 0 and: { snapDistance > 0 }) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(input.lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
						}
						{ inputMapping.mapping === \linbicurve } {
							if (inputMapping.curve != 0 and: { snapDistance > 0 }) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(input.linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
						}
						{ inputMapping.mapping === \linenv } {
							if (snapDistance > 0) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(input.linenv(env: inputMapping.env))
						}
						{ inputMapping.mapping === \explin } {
							if (snapDistance > 0) {
								this.setMidiSnapDistance(0)
							};
							cv.input_((input+1).explin(1, 2, 0, 1))
						}
						{ inputMapping.mapping === \expexp or: {inputMapping.mapping === \linexp }} {
							if (this.widget.getSpec.hasZeroCrossing and: { this.getMidiInputMapping !== \linlin}) {
								this.setMidiInputMapping(\linlin);
								cv.input_(input.linlin(0, 1, 0, 1))
							} {
								if (snapDistance > 0) {
									this.setMidiSnapDistance(0)
								};
								cv.value_((input+1).perform(inputMapping.mapping, 1, 2, this.widget.getSpec.minval, this.widget.getSpec.maxval))
							}
						}
						{
							cv.input_(input);
						}
					};
					// avoid jumps if another endless connection exists
					accum[this.widget] = cv.input;
				},
				// endless mode
				1, {
					// "midiMode is endless".postln;
					// we can't use cv.input (range: 0-1) in curved ramps or enveloped ramps
					// accumulation must happen within a linear ramp
					accum[this.widget] = accum[this.widget] + (val-this.getMidiZero/127*this.getMidiResolution);

					// accumulation is by default not limited like cv.input
					case
					{ accum[this.widget] < 0 } { accum[this.widget] = 0 }
					{ accum[this.widget] > 1 } { accum[this.widget] = 1 };

					case
					{ inputMapping.mapping === \lincurve } {
						cv.input_(accum[this.widget].lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
					}
					{ inputMapping.mapping === \linbicurve } {
						cv.input_(accum[this.widget].linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
					}
					{ inputMapping.mapping === \linenv } {
						cv.input_(accum[this.widget].linenv(env: inputMapping.env))
					}
					{ inputMapping.mapping === \explin } {
						cv.input_((accum[this.widget]+1).explin(1, 2, 0, 1))
					}
					{ inputMapping.mapping === \expexp or: { inputMapping.mapping === \linexp }} {
						if (this.widget.getSpec.hasZeroCrossing) {
							this.setMidiInputMapping(\linlin);
							cv.input_(accum[this.widget])
						} {
							cv.value_((accum[this.widget]+1).perform(inputMapping.mapping, 1, 2, this.widget.getSpec.minval, this.widget.getSpec.maxval))
						}
					}
					{ cv.input_(accum[this.widget]) };
				}
			);
		};

		learn = cc.isNil and: { ch.isNil  and: {src.isNil }};
		if (learn) {
			"MIDIFunc at widget.wmc.midiConnections.m.value[%] should learn".format(index).inform;
			makeFunc.().learnSync(this.widget, index: index);
		} {
			"MIDIFunc at widget.wmc.midiConnections.m.value[%] was set to src: %, channel: %, number: %".format(
				index, src, ch, cc
			).inform;
			makeFunc.(src, ch, cc, t, d);
		};
		accum[this.widget] = cv.input;
		^this.widget.wmc.midiConnections.m.value[index]
	}

	remove { |forceAll = false|
		var mc = this.widget.wmc;
		var index = this.index;

		if (mc.midiConnectors.m.value.size > 1 or: { forceAll }) {
			this.midiDisconnect;
			[
				mc.midiOptions.m.value,
				mc.midiConnections.m.value,
				mc.midiDisplay.m.value,
				mc.midiConnectorNames.m.value
			].do(_.removeAt(index));
			mc.midiConnectors.m.value.remove(this);
			mc.midiConnectors.m.changedPerformKeys(this.widget.syncKeys, index);
			// set editor elements (and other custom elements depending
			// on mc.midiConnectors.m.value) to suitable connector
			onConnectorRemove.value(this.widget, index);
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [this.widget.name.cs, this.name] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}
