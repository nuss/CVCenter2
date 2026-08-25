MidiConnectorMS : AbstractConnector {
	classvar <accum;
	classvar <onConnectorRemove;

	var <slot;

	*initClass {
		accum = ();
	}

	*new { |widget, name, slot|
		if (widget.class === Symbol or: { widget.isString }) {
			widget = CVWidget.all[widget.asSymbol]
		};
		if (widget.isNil or: {
			widget.class !== CVWidgetMS
		}) {
			Error("An MidiConnectorMS can only be created for an existing CVWidgetMS").throw;
		};
		if (slot.isNil or: { slot.isNumber.not }) {
			"Please provide a numeric slot for a new MidiConnectorMS!".error;
			^nil
		};
		^super.newCopyArgs(widget, slot.asInteger).init(name);
	}

	*onConnectorRemove_ { |func|
		onConnectorRemove = onConnectorRemove.addFunc(func)
	}

	init { |name|
		this.widget.numMidiConnectors[this.slot] = this.widget.numMidiConnectors[this.slot] + 1;
		name ?? {
			name = "MIDI Connection %".format(this.widget.numMidiConnectors[this.slot]).asSymbol;
		};

		this.initModels(this.widget.wmc, name);

		this.widget.wmc.midiConnectors.m[this.slot].value_(
			this.widget.wmc.midiConnectors.m[this.slot].value.add(this)
		).changedPerformKeys(this.widget.syncKeys);
	}

	initModels { |wmc, name|
		var size = this.widget.size;

		wmc.midiConnections ?? { wmc.midiConnections = () };
		wmc.midiConnections.m ?? {
			wmc.midiConnections.m = List.newClear(size);
		};
		wmc.midiConnections.m[this.slot] ?? {
			wmc.midiConnections.m[this.slot] = Ref(List[])
		};
		wmc.midiConnections.m[this.slot].value.add(nil);

		wmc.midiOptions ?? { wmc.midiOptions = () };
		wmc.midiOptions.m ?? {
			wmc.midiOptions.m = List.newClear(size);
		};
		wmc.midiOptions.m[this.slot] ?? {
			wmc.midiOptions.m[this.slot] = Ref(List[])
		};
		wmc.midiOptions.m[this.slot].value.add((
			midiMode: CVWidget.midiMode,
			midiZero: CVWidget.midiZero,
			ctrlButtonGroup: CVWidget.midiCtrlButtonGroup,
			midiResolution: CVWidget.resolution,
			snapDistance: CVWidget.snapDistance,
			// special case: a classvar getter/setter can only be defined as a literal
			midiInputMapping: CVWidget.inputMapping
		));

		wmc.midiDisplay ?? { wmc.midiDisplay = () };
		wmc.midiDisplay.m ?? {
			wmc.midiDisplay.m = List.newClear(size)
		};
		wmc.midiDisplay.m[this.slot] ?? {
			wmc.midiDisplay.m[this.slot] = Ref(List[])
		};
		wmc.midiDisplay.m[this.slot].value.add((
			src: 'source...',
			chan: "chan",
			ctrl: "ctrl",
			learn: "L",
			toolTip: "Click and move hardware slider/knob to connect to",
			slotToolTip: "Select the the CVWidgetMS's '%' slot (widget has % slots)."
		));

		wmc.midiConnectorNames ?? { wmc.midiConnectorNames = () };
		wmc.midiConnectorNames.m ?? {
			wmc.midiConnectorNames.m = List.newClear(size)
		};
		wmc.midiConnectorNames.m[this.slot] ?? {
			wmc.midiConnectorNames.m[this.slot] = Ref(List[])
		};
		wmc.midiConnectorNames.m[this.slot].value.add(name);

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

	prInitMidiConnectors { |mc, cv|
		mc.midiConnectors.c ?? {
			mc.midiConnectors.c = List.newClear(this.widget.size)
		};
		mc.midiConnectors.c[this.slot] = SimpleController(mc.midiConnectors.m[this.slot]);
		mc.midiConnectors.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	prInitMidiOptions { |mc, cv|
		mc.midiOptions.c ?? {
			mc.midiOptions.c = List.newClear(this.widget.size)
		};
		mc.midiOptions.c[this.slot] = SimpleController(mc.midiOptions.m[this.slot]);
		mc.midiOptions.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// var index = mc.midiConnectors.m[this.slot].value.indexOf(this);
			// do something...
		})
	}

	prInitMidiConnection { |mc, cv|
		mc.midiConnections.c ?? {
			mc.midiConnections.c = List.newClear(this.widget.size)
		};
		mc.midiConnections.c[this.slot] = SimpleController(mc.midiConnections.m[this.slot]);
		mc.midiConnections.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// do something...
		})
	}

	prInitMidiDisplay { |mc, cv|
		mc.midiDisplay.c ?? {
			mc.midiDisplay.c = List.newClear(this.widget.size)
		};
		mc.midiDisplay.c[this.slot] = SimpleController(mc.midiDisplay.m[this.slot]);
		mc.midiDisplay.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// "midiDisplay.c.triggered".postln;
			// 	var index = mc.midiConnectors.m[this.slot].value.indexOf(this);
			// 	// "midiDisplay.c - changer.value: %, moreArgs: %".format(changer.value, index).postln;
			// 	// ...
		})
	}

	prInitMidiConnectorNames { |mc, cv|
		mc.midiConnectorNames.c ?? {
			mc.midiConnectorNames.c = List.newClear(this.widget.size)
		};
		mc.midiConnectorNames.c[this.slot] = SimpleController(mc.midiConnectorNames.m[this.slot]);
		mc.midiConnectorNames.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// 	"midiConnectorNames.c triggered:\n\t%\n\t%\n\t%".format(changer.value, what, moreArgs).postln;
		})
	}

	index {
		^this.widget.midiConnectors[this.slot].value.indexOf(this)
	}

	name {
		^this.widget.wmc.midiConnectorNames.m[this.slot].value[this.index]
	}

	name_ { |name|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.midiConnectorNames.m[this.slot].value[index] = name.asSymbol;
		mc.midiConnectorNames.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
	}

	setMidiOption { |option, value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.midiOptions.m[this.slot].value[index][option] = value;
		mc.midiOptions.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
	}

	getMidiOption { |option|
		var index = this.index;
		^this.widget.wmc.midiOptions.m[this.slot].value[index][option]
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
		// mc.midiOptions.m[this.slot].value[index].midiInputMapping.mapping = mapping;
		mc.midiOptions.m[this.slot].value[index].midiInputMapping_((mapping: mapping));
		case
		{ mapping === \lincurve or: { mapping === \linbicurve }} {
			mc.midiOptions.m[this.slot].value[index].midiInputMapping.curve = curve;
		}
		{ mapping === \linenv } {
			mc.midiOptions.m[this.slot].value[index].midiInputMapping.env = env;
		};
		mc.midiOptions.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
	}

	setMidiDisplay { |displayValueName, value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.midiDisplay.m[this.slot].value[index][displayValueName] = value;
		mc.midiDisplay.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
	}

	getMidiDisplay { |displayValueName|
		var index = this.index;
		^this.widget.wmc.midiDisplay.m[this.slot].value[index][displayValueName]
	}

	getSlotToolTip {
		^this.getMidiDisplay(\slotToolTip)
	}

	setMIDIFuncEnabled { |boolEnabled|
		var index = this.index;
		var m = this.widget.wmc.midiConnections.m[this.slot];
		if (m.value[index].isNil) {
			"connector at index % is currently not connected.".format(index).inform
		} {
			if (boolEnabled) { m.value[index].enable } { m.value[index].disable };
			m.changedPerformKeys(this.widget.syncKeys, index);
		}
	}

	getMIDIFuncEnabled {
		if (this.widget.wmc.midiConnections.m[this.slot].value[this.index].notNil) {
			^this.widget.wmc.midiConnections.m[this.slot].value[this.index].enabled
		} { ^true }
	}

	midiConnect { |num, chan, srcID, argTemplate, dispatcher|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.midiConnections.m[this.slot].value[index] = this.prMIDIFunc(index, num, chan, srcID, argTemplate, dispatcher);
		mc.midiConnections.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
		mc.midiDisplay.m[this.slot].value[index].learn = "X";
		mc.midiDisplay.m[this.slot].value[index].toolTip = "Click to disconnect";
		mc.midiConnections.m[this.slot].value[index].srcID !? {
			mc.midiDisplay.m[this.slot].value[index].src = mc.midiConnections.m[this.slot].value[index].srcID
		};
		mc.midiConnections.m[this.slot].value[index].chan !? {
			mc.midiDisplay.m[this.slot].value[index].chan = mc.midiConnections.m[this.slot].value[index].chan
		};
		mc.midiConnections.m[this.slot].value[index].msgNum !? {
			mc.midiDisplay.m[this.slot].value[index].ctrl = mc.midiConnections.m[this.slot].value[index].msgNum
		};
		mc.midiConnections.m[this.slot].value[index].argTemplate !? {
			mc.midiDisplay.m[this.slot].value[index].template = mc.midiConnections.m[this.slot].value[index].argTemplate.cs
		};
		mc.midiConnections.m[this.slot].value[index].dispatcher !? {
			mc.midiDisplay.m[this.slot].value[index].dispatcher = mc.midiConnections.m[this.slot].value[index].dispatcher.cs
		};
		mc.midiDisplay.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? { this.midiDisconnect }
		})
	}

	midiDisconnect {
		var mc = this.widget.wmc;
		var index = this.index;
		mc.midiConnections.m[this.slot].value[index].free;
		mc.midiConnections.m[this.slot].value[index] = nil;
		mc.midiConnections.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
		mc.midiDisplay.m[this.slot].value[index].src = 'source...';
		mc.midiDisplay.m[this.slot].value[index].chan = "chan";
		mc.midiDisplay.m[this.slot].value[index].ctrl = "ctrl";
		mc.midiDisplay.m[this.slot].value[index].template = nil;
		mc.midiDisplay.m[this.slot].value[index].dispatcher = nil;
		mc.midiDisplay.m[this.slot].value[index].toolTip = "Click and move hardware slider/knob to connect to";
		mc.midiDisplay.m[this.slot].value[index].learn = "L";
		mc.midiDisplay.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
		CmdPeriod.remove({
			this.widget !? { this.midiDisconnect }
		})
	}

	prMIDIFunc { |index, cc, ch, src, t, d|
		var snapDistance, inputMapping, input;
		var cv = this.widget.cv;
		var makeFunc = { |argSrc, argChan, argNum, argTempl, argDispatcher|
			if (this.widget.wmc.midiConnections.m[this.slot].value[index].isNil or: {
				this.widget.wmc.midiConnections.m[this.slot].value[index].func.isNil
			}) {
				this.widget.wmc.midiConnections.m[this.slot].value[index] = MIDIFunc.cc(
					ccAction,
					ccNum: argNum !? { argNum.asInteger },
					chan: argChan !? { argChan.asInteger },
					srcID: argSrc !? { argSrc.asInteger },
					argTemplate: argTempl,
					dispatcher: argDispatcher
				)
			};
			this.widget.wmc.midiConnections.m[this.slot].value[index]
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
						input < (cv.input[this.slot] + (snapDistance)) and: {
							input > (cv.input[this.slot] - (snapDistance))
					})) {
						case
						{ inputMapping.mapping === \lincurve } {
							if (inputMapping.curve != 0 and: { snapDistance > 0 }) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(
								cv.input[..this.slot-1] ++
								input.lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve) ++
								cv.input[this.slot+1..]
							)
						}
						{ inputMapping.mapping === \linbicurve } {
							if (inputMapping.curve != 0 and: { snapDistance > 0 }) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(
								cv.input[..this.slot-1] ++
								input.linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve) ++
								cv.input[this.slot+1..]
							)
						}
						{ inputMapping.mapping === \linenv } {
							if (snapDistance > 0) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(
								cv.input[..this.slot+1] ++
								input.linenv(env: inputMapping.env) ++
								cv.input[this.slot+1..]
							)
						}
						{ inputMapping.mapping === \explin } {
							if (snapDistance > 0) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(
								cv.input[..this.slot-1] ++
								(input+1).explin(1, 2, 0, 1) ++
								cv.input[this.slot+1..]
							)
						}
						{ inputMapping.mapping === \expexp or: {inputMapping.mapping === \linexp }} {
							if (this.widget.getSpec.hasZeroCrossing and: { this.getMidiInputMapping !== \linlin}) {
								this.setMidiInputMapping(\linlin);
								cv.input_(
									cv.input[..this.slot-1] ++
									input.linlin(0, 1, 0, 1) ++
									cv.input[this.slot+1..]
								)
							} {
								if (snapDistance > 0) {
									this.setMidiSnapDistance(0)
								};
								cv.value_(
									cv.value[..this.slot-1] ++
									(input+1).perform(
										inputMapping.mapping, 1, 2,
										this.widget.getSpec.minval.asArray.wrapAt(this.slot),
										this.widget.getSpec.maxval.asArray.wrapAt(this.slot)
									) ++
									cv.value[this.slot+1..]
								)
							}
						}
						{
							cv.input_(
								cv.input[..this.slot-1] ++
								input ++
								cv.input[this.slot+1..]
							);
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
					accum[this.widget][this.slot] = accum[this.widget][this.slot] + (val-this.getMidiZero/127*this.getMidiResolution);

					// accumulation is by default not limited like cv.input
					case
					{ accum[this.widget][this.slot] < 0 } { accum[this.widget][this.slot] = 0 }
					{ accum[this.widget][this.slot] > 1 } { accum[this.widget][this.slot] = 1 };

					case
					{ inputMapping.mapping === \lincurve } {
						cv.input_(
							cv.input[..this.slot-1] ++
							accum[this.widget].lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve) ++
							cv.input[this.slot+1..]
						)
					}
					{ inputMapping.mapping === \linbicurve } {
						cv.input_(
							cv.input[..this.slot-1] ++
							accum[this.widget].linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve) ++
							cv.input[this.slot+1..]
						)
					}
					{ inputMapping.mapping === \linenv } {
						cv.input_(
							cv.input[..this.slot-1] ++
							accum[this.widget].linenv(env: inputMapping.env) ++
							cv.input[this.slot+1..]
						)
					}
					{ inputMapping.mapping === \explin } {
						cv.input_(
							cv.input[..this.slot-1] ++
							(accum[this.widget][this.slot]+1).explin(1, 2, 0, 1) ++
							cv.input[this.slot+1..]
						)
					}
					{ inputMapping.mapping === \expexp or: { inputMapping.mapping === \linexp }} {
						if (this.widget.getSpec.hasZeroCrossing) {
							this.setMidiInputMapping(\linlin);
							cv.input_(
								cv.input[..this.slot-1] ++
								accum[this.widget][this.slot] ++
								cv.input[this.slot+1..]
							)
						} {
							cv.value_(
								cv.value[..this.slot-1] ++
								(accum[this.widget]+1).perform(
									inputMapping.mapping, 1, 2,
									this.widget.getSpec.minval.asArray.wrapAt(this.slot),
									this.widget.getSpec.maxval.asArray.wrapAt(this.slot)
								) ++
								cv.value[this.slot+1..]
							)
						}
					}
					{ cv.input_(accum[this.widget]) };
				}
			);
		};

		if (cc.isNil and: { ch.isNil  and: {src.isNil }}) {
			"MIDIFunc at widget.wmc.midiConnections.m[this.slot].value[%] should learn".format(index).inform;
			makeFunc.().learnSync(this.widget, this.slot, index);
		} {
			"MIDIFunc at widget.wmc.midiConnections.m[this.slot].value[%] was set to src: %, channel: %, number: %".format(
				index, src, ch, cc
			).inform;
			makeFunc.(src, ch, cc, t, d);
		};
		accum[this.widget] = cv.input;
		^this.widget.wmc.midiConnections.m[this.slot].value[index]
	}

	remove { |forceAll = false|
		var mc = this.widget.wmc;
		var index = this.index;

		if (mc.midiConnectors.m[this.slot].value.size > 1 or: { forceAll }) {
			this.midiDisconnect;
			[
				mc.midiOptions.m[this.slot].value,
				mc.midiConnections.m[this.slot].value,
				mc.midiDisplay.m[this.slot].value,
				mc.midiConnectorNames.m[this.slot].value
			].do(_.removeAt(index));
			mc.midiConnectors.m[this.slot].value.remove(this);
			mc.midiConnectors.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
			// set editor elements (and other custom elements depending
			// on mc.midiConnectors.m[this.slot].value) to suitable connector
			onConnectorRemove.value(this.widget, index);
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [this.widget.name.cs, this.name, this.slot] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}