OscConnector {
	classvar cAnons = 0;
	var <widget;

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("An OscConnector can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget).init(name);
	}

	init { |name|
		widget.numOscConnectors = widget.numOscConnectors + 1;
		name ?? {
			name = "OSC Connection %".format(widget.numOscConnectors).asSymbol;
		};
		this.initModels(widget.wmc, name);
		widget.wmc.oscConnectors.model.value_(
			widget.wmc.oscConnectors.model.value.add(this)
		).changedPerformKeys(widget.syncKeys);
	}

	initModels { |wmc, name|
		wmc.oscCalibration ?? { wmc.oscCalibration = () };
		wmc.oscCalibration.model ?? {
			wmc.oscCalibration.model = Ref(List[]);
		};
		wmc.oscCalibration.model.value.add(CVWidget.oscCalibration);

		wmc.oscInputRange ?? { wmc.oscInputRange = () };
		wmc.oscInputRange.model ?? {
			wmc.oscInputRange.model = Ref(List[]);
		};
		wmc.oscInputRange.model.value.add([0.0001, 0.0001]);

		wmc.oscConnections ?? { wmc.oscConnections = () };
		wmc.oscConnections.model ?? {
			wmc.oscConnections.model = Ref(List[]);
		};
		wmc.oscConnections.model.value.add(false);

		wmc.oscDisplay ?? { wmc.oscDisplay = () };
		wmc.oscDisplay.model ?? {
			wmc.oscDisplay.model = Ref(List[]);
		};
		wmc.oscDisplay.model.value.add((
			ipField: nil,
			portField: nil,
			nameField: "/my/cmd/name",
			index: 1,
			connectorButVal: 0,
			editEnabled: true
		));

		wmc.oscConnectorNames ?? { wmc.oscConnectorNames = () };
		wmc.oscConnectorNames.model ?? {
			wmc.oscConnectorNames.model = Ref(List[]);
		};
		wmc.oscConnectorNames.model.value.add(name);

		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitOscCalibration,
			prInitOscInputRange,
			prInitOscConnection,
			prInitOscDisplay,
			prInitOscConnectors,
			prInitOscConnectorNames
		].do { |method|
			this.perform(method, wmc, widget.cv)
		}
	}

	prInitOscConnectors { |mc, cv|
		mc.oscConnectors.controller ?? {
			mc.oscConnectors.controller = SimpleController(mc.oscConnectors.model)
		};
		mc.oscConnectors.controller.put(\default, { |changer, what ... moreArgs|
			"blablabla, do something..."
		})
	}

	prInitOscCalibration { |mc, cv|
		mc.oscCalibration.controller ?? {
			mc.oscCalibration.controller = SimpleController(mc.oscCalibration.model)
		};
		mc.oscCalibration.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscInputRange { |mc, cv|
		mc.oscInputRange.controller ?? {
			mc.oscInputRange.controller = SimpleController(mc.oscInputRange.model)
		};
		mc.oscInputRange.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscConnection { |mc, cv|
		mc.oscConnections.controller ?? {
			mc.oscConnections.controller = SimpleController(mc.oscConnections.model)
		};
		mc.oscConnections.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscDisplay { |mc, cv|
		mc.oscDisplay.controller ?? {
			mc.oscDisplay.controller = SimpleController(mc.oscDisplay.model)
		};
		mc.oscDisplay.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscConnectorNames { |mc, cv|
		mc.oscConnectorNames.controller ?? {
			mc.oscConnectorNames.controller = SimpleController(mc.oscConnectorNames.model)
		};
		mc.oscConnectorNames.controller.put(\default, { |changer, what, moreArgs|

		})
	}

	name {
		var conID = widget.wmc.oscConnectors.model.value.indexOf(this);
		if (conID.notNil) {
			^widget.wmc.oscConnectorNames.model.value[conID]
		} { ^nil };
	}

	name_ { |name|
		var conID = widget.wmc.oscConnectors.model.value.indexOf(this);
		conID !? {
			widget.wmc.oscConnectorNames.model.value[conID] = name.asSymbol;
			widget.wmc.oscConnectorNames.model.changedPerformKeys(widget.syncKeys, conID);
		}
	}

	remove {
		// remove views, OSCdefs...
	}

	oscConnect {}
	oscDisconnect {}

}

MidiConnector {
	classvar cAnons = 0;
	classvar allMidiFuncs;
	classvar accum;
	classvar <onConnectorRemove;
	var <widget;


	*initClass {
		allMidiFuncs = ();
		// input accumulation of input in a linear range in 'endless' mode
		// see ccAction in prInitMidiConnection
		accum = ();
	}

	*onConnectorRemove_ { |func|
		onConnectorRemove = onConnectorRemove.addFunc(func)
	}

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("A MidiConnector can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget).init(name);
	}

	init { |name|
		widget.numMidiConnectors = widget.numMidiConnectors + 1;
		name ?? {
			name = "MIDI Connection %".format(widget.numMidiConnectors).asSymbol;
		};
		this.initModels(widget.wmc, name);
		widget.wmc.midiConnectors.model.value_(
			widget.wmc.midiConnectors.model.value.add(this)
		).changedPerformKeys(widget.syncKeys);

		allMidiFuncs[widget] ?? {
			allMidiFuncs.put(widget, List[])
		};
		allMidiFuncs[widget].add(nil);
	}

	initModels { |wmc, name|
		wmc.midiOptions ?? { wmc.midiOptions = () };
		wmc.midiOptions.model ?? {
			wmc.midiOptions.model = Ref(List[]);
		};
		wmc.midiOptions.model.value.add((
			midiMode: CVWidget.midiMode,
			midiZero: CVWidget.midiZero,
			ctrlButtonGroup: CVWidget.ctrlButtonGroup,
			midiResolution: CVWidget.midiResolution,
			snapDistance: CVWidget.snapDistance
		));

		wmc.midiConnections ?? { wmc.midiConnections = () };
		wmc.midiConnections.model ?? {
			wmc.midiConnections.model = Ref(List[]);
		};
		wmc.midiConnections.model.value.add(nil);

		wmc.midiInputMappings ?? { wmc.midiInputMappings = () };
		wmc.midiInputMappings.model ?? {
			wmc.midiInputMappings.model = Ref(List[]);
		};
		wmc.midiInputMappings.model.value.add((mapping: \linlin));

		wmc.midiDisplay ?? { wmc.midiDisplay = () };
		wmc.midiDisplay.model ?? {
			wmc.midiDisplay.model = Ref(List[]);
		};
		wmc.midiDisplay.model.value.add((
			src: 'source...',
			chan: "chan",
			ctrl: "ctrl",
			learn: "L",
			toolTip: "Click and move hardware slider/knob to connect to"
		));
		wmc.midiConnectorNames ?? { wmc.midiConnectorNames = () };
		wmc.midiConnectorNames.model ?? {
			wmc.midiConnectorNames.model = Ref(List[]);
		};
		wmc.midiConnectorNames.model.value.add(name);
		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitMidiConnectors,
			prInitMidiConnectorNames,
			prInitMidiOptions,
			prInitMidiInputMappings,
			prInitMidiConnection,
			prInitMidiDisplay
		].do { |method|
			this.perform(method, wmc, widget.cv)
		}
	}

	// private: default controllers
	prInitMidiConnectors { |mc, cv|
		mc.midiConnectors.controller ?? {
			mc.midiConnectors.controller = SimpleController(mc.midiConnectors.model);
		};
		mc.midiConnectors.controller.put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	prInitMidiOptions { |mc, cv|
		mc.midiOptions.controller ?? {
			mc.midiOptions.controller = SimpleController(mc.midiOptions.model);
		};
		mc.midiOptions.controller.put(\default, { |changer, what ... moreArgs|
			var index = mc.midiConnectors.model.value.indexOf(this);
		})
	}

	prInitMidiConnection { |mc, cv|
		var ccAction, makeCCconnection;
		var slotChanger;
		var updateModelsFunc = { |num, chan, src, index|
			mc.midiConnections.model.value[index] = (num: num, chan: chan, src: src);
			mc.midiDisplay.model.value[index] = (
				learn: "X",
				src: src ? 'source...',
				chan: chan ? "chan",
				ctrl: num ? "ctrl",
				toolTip: "Click to disconnect"
			);
			mc.midiDisplay.model.changedPerformKeys(widget.syncKeys, index);
		};

		mc.midiConnections.controller ?? {
			mc.midiConnections.controller = SimpleController(mc.midiConnections.model);
		};
		mc.midiConnections.controller.put(\default, { |changer, what ... moreArgs|
			var index = moreArgs[0];
			// brute force fix - why is 'this' not considered correctly?
			var self = mc.midiConnectors.model.value[index];
			var inputMapping, input;
			// for endless mode we're going to perate on a linearly in-/decremented value,
			// starting at the CV's current input (value normalized from 0 to 1)

			accum[widget] ?? { accum[widget] = cv.input };

			if (changer[index].value.class == Event) {
				slotChanger = changer[index].value;
				// midiConnect
				updateModelsFunc.(slotChanger.num, slotChanger.chan, slotChanger.src, index);
				ccAction = { |val, num, chan, src|
					// MIDI learn
					// we must infer the connections parameters here
					if (mc.midiConnections.model.value[index].isEmpty) { updateModelsFunc.(num, chan, src, index) };
					inputMapping = self.getMidiInputMapping;
					self.getMidiMode.switch(
						//  0-127
						0, {
							input = val/127;
							if ((self.getSnapDistance <= 0).or(
								input < (cv.input + (self.getSnapDistance/2)) and: {
									input > (cv.input - (self.getSnapDistance/2))
							})) {
								case
								{ inputMapping.mapping === \lincurve } {
									if (inputMapping.curve != 0 and: { self.getSnapDistance > 0 }) {
										self.setSnapDistance(0)
									};
									cv.input_(input.lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
								}
								{ inputMapping.mapping === \linbicurve } {
									if (inputMapping.curve != 0 and: { self.getSnapDistance > 0 }) {
										self.setSnapDistance(0)
									};
									cv.input_(input.linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
								}
								{ inputMapping.mapping === \linenv } {
									if (self.getSnapDistance > 0) {
										self.setSnapDistance(0)
									};
									cv.input_(input.linenv(env: inputMapping.env))
								}
								{ inputMapping.mapping === \explin } {
									if (self.getSnapDistance > 0) {
										self.setSnapDistance(0)
									};
									cv.input_((input+1).explin(1, 2, 0, 1))
								}
								{ inputMapping.mapping === \expexp or: {inputMapping.mapping === \linexp }} {
									if (widget.getSpec.hasZeroCrossing and: { self.getMidiInputMapping !== \linlin}) {
										self.setMidiInputMapping(\linlin);
										cv.input_(input.linlin(0, 1, 0, 1))
									} {
										if (self.getSnapDistance > 0) {
											self.setSnapDistance(0)
										};
										cv.value_((input+1).perform(inputMapping.mapping, 1, 2, widget.getSpec.minval, widget.getSpec.maxval))
									}
								}
								{
									cv.input_(input);
								}
							};
							// avoid jumps if another endless connection exists
							accum[widget] = cv.input;
						},
						// endless mode
						1, {
							// "midiMode is endless".postln;
							// we can't use cv.input (range: 0-1) in curved ramps or enveloped ramps
							// accumulation must happen within a linear ramp
							accum[widget] = accum[widget] + (val-self.getMidiZero/127*self.getMidiResolution);

							// accumulation is by default not limited like cv.input
							case
							{ accum[widget] < 0 } { accum[widget] = 0 }
							{ accum[widget] > 1 } { accum[widget] = 1 };

							case
							{ inputMapping.mapping === \lincurve } {
								cv.input_(accum[widget].lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
							}
							{ inputMapping.mapping === \linbicurve } {
								cv.input_(accum[widget].linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
							}
							{ inputMapping.mapping === \linenv } {
								cv.input_(accum[widget].linenv(env: inputMapping.env))
							}
							{ inputMapping.mapping === \explin } {
								cv.input_((accum[widget]+1).explin(1, 2, 0, 1))
							}
							{ inputMapping.mapping === \expexp or: {inputMapping.mapping === \linexp }} {
								if (widget.getSpec.hasZeroCrossing) {
									self.setMidiInputMapping(\linlin);
									cv.input_(accum[widget])
								} {
									cv.value_((accum[widget]+1).expexp(1, 2, widget.getSpec.minval, widget.getSpec.maxval))
								}
							}
							{ cv.input_(accum[widget]) };
						}
					);
					// [val/127, cv.input, cv.value].postln;
				};
				makeCCconnection = { |argSrc, argChan, argNum|
					if (allMidiFuncs[widget][index].isNil or: {
						allMidiFuncs[widget][index].func.isNil
					}) {
						allMidiFuncs[widget][index] = MIDIFunc.cc(
							ccAction,
							ccNum: argNum !? { argNum.asInteger },
							chan: argChan !? { argChan.asInteger },
							srcID: argSrc !? { argSrc.asInteger }
						);
					};
					allMidiFuncs[widget][index];
				};

				if (slotChanger.isEmpty) {
					"allMidiFuncs[widget], connector %, should learn".format(index).inform;
					makeCCconnection.().learn;
				} {
					"allMidiFuncs[widget], connector %, was set to src: %, channel: %, number: %".format(
						index, slotChanger.src, slotChanger.chan, slotChanger.num
					).inform;
					makeCCconnection.(slotChanger.src, slotChanger.chan, slotChanger.num);
				};
			} {
				mc.midiDisplay.model.value[index] = (
					learn: "L",
					src: 'source...',
					chan: "chan",
					ctrl: "ctrl",
					toolTip: "Click and move hardware slider/knob to connect to"
				);
				mc.midiDisplay.model.changedPerformKeys(widget.syncKeys, index);
				allMidiFuncs[widget][index].clear;
				if (mc.midiConnections.model.value.select(_.notNil).isEmpty) {
					// no conections for widget, uninitialize accum[widget]
					accum[widget] !? { accum[widget] = nil };
				}
			};
		})
	}

	prInitMidiInputMappings { |mc, cv|
		mc.midiInputMappings.controller ?? {
			mc.midiInputMappings.controller = SimpleController(mc.midiInputMappings.model);
		};
		mc.midiInputMappings.controller.put(\default, { |changer, what ... moreArgs|
			// "yadda yadda: %, %, %".format(changer.value, what, moreArgs).postln;
		})
	}

	prInitMidiDisplay { |mc, cv|
		mc.midiDisplay.controller ?? {
			mc.midiDisplay.controller = SimpleController(mc.midiDisplay.model);
		};
		mc.midiDisplay.controller.put(\default, { |changer, what ... moreArgs|
			// "midiDisplay.controller.triggered".postln;
			// 	var index = mc.midiConnectors.model.value.indexOf(this);
			// 	// "midiDisplay.controller - changer.value: %, moreArgs: %".format(changer.value, index).postln;
			// 	// ...
		})
	}

	prInitMidiConnectorNames { |mc, cv|
		mc.midiConnectorNames.controller ?? {
			mc.midiConnectorNames.controller = SimpleController(mc.midiConnectorNames.model);
		};
		mc.midiConnectorNames.controller.put(\default, { |changer, what ... moreArgs|
			// 	"midiConnectorNames.controller triggered:\n\t%\n\t%\n\t%".format(changer.value, what, moreArgs).postln;
		})
	}

	name {
		var mc = widget.wmc;
		var conID = mc.midiConnectors.model.value.indexOf(this);
		if (conID.notNil) {
			^widget.wmc.midiConnectorNames.model.value[conID]
		} { ^nil }
	}

	name_ { |name|
		var mc = widget.wmc;
		var conID = mc.midiConnectors.model.value.indexOf(this);
		conID !? {
			widget.wmc.midiConnectorNames.model.value[conID] = name.asSymbol;
			widget.wmc.midiConnectorNames.model.changedPerformKeys(widget.syncKeys, conID);
		}
	}

	setMidiMode { |mode|
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};
		mc.midiOptions.model.value[index].midiMode = mode;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiMode {
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		^mc.midiOptions.model.value[index].midiMode;
	}

	setMidiZero { |zeroval|
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		zeroval = zeroval.asInteger;
		mc.midiOptions.model.value[index].midiZero = zeroval;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiZero {
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		^mc.midiOptions.model.value[index].midiZero;
	}

	setSnapDistance { |snapDistance|
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		snapDistance = snapDistance.asFloat;
		mc.midiOptions.model.value[index].snapDistance = snapDistance;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getSnapDistance {
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		^mc.midiOptions.model.value[index].snapDistance;
	}

	setCtrlButtonGroup { |numButtons|
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		if (numButtons.notNil and:{ numButtons.isInteger.not }) {
			Error("setCtrlButtonGroup: 'numButtons' must either be an Integer or nil!").throw;
		};
		mc.midiOptions.model.value[index].ctrlButtonGroup = numButtons;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getCtrlButtonGroup {
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		^mc.midiOptions.model.value[index].ctrlButtonGroup;
	}

	setMidiResolution { |resolution|
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		mc.midiOptions.model.value[index].midiResolution = resolution;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiResolution {
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		^widget.wmc.midiOptions.model.value[index].midiResolution;
	}

	setMidiInputMapping { |mapping, curve = 0, env(Env([0, 1], [1]))|
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		mapping = mapping.asSymbol;
		[\linlin, \linexp, \explin, \expexp, \lincurve, \linbicurve, \linenv].indexOf(mapping) ?? {
			"arg 'mapping' must be one of \\linlin, \\linexp, \\explin, \\expexp, \\lincurve, \\linbicurve or \\linenv".error;
			^nil
		};
		mc.midiInputMappings.model.value[index].mapping = mapping;
		case
		{ mapping === \lincurve or: { mapping === \linbicurve }} {
			mc.midiInputMappings.model.value[index].curve = curve;
			mc.midiInputMappings.model.value[index].env = nil;
		}
		{ mapping === \linenv } {
			mc.midiInputMappings.model.value[index].curve = nil;
			mc.midiInputMappings.model.value[index].env = env;
		}
		{
			mc.midiInputMappings.model.value[index].curve = nil;
			mc.midiInputMappings.model.value[index].env = nil;
		};
		mc.midiInputMappings.model.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiInputMapping {
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		^widget.wmc.midiInputMappings.model.value[index];
	}

	midiConnect { |src, chan, num|
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		mc.midiConnections.model.value[index] = (src: src, chan: chan, num: num);
		mc.midiConnections.model.changedPerformKeys(widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? {
				allMidiFuncs[widget][index].permanent_(this.widget.class.removeResponders)
			}
		})
	}

	midiDisconnect {
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		mc.midiConnections.model.value[index] = nil;
		mc.midiConnections.model.changedPerformKeys(widget.syncKeys, index);
	}

	remove { |forceAll = false|
		var mc = widget.wmc;
		var index = mc.midiConnectors.model.value.indexOf(this);
		var names, allMS;

		if (mc.midiConnectors.model.value.size > 1 or: { forceAll }) {
			this.midiDisconnect;
			allMidiFuncs[widget][index].free;
			allMidiFuncs[widget].removeAt(index);
			[
				widget.wmc.midiOptions.model.value,
				widget.wmc.midiConnections.model.value,
				widget.wmc.midiDisplay.model.value,
				widget.wmc.midiConnectorNames.model.value,
				widget.wmc.midiInputMappings.model.value
			].do(_.removeAt(index));
			mc.midiConnectors.model.value.remove(this);
			mc.midiConnectors.model.value.changed(\value);
			// set editor elements (and other custom elements depending
			// on mc.midiConnectors.model.value) to suitable connector
			onConnectorRemove.value(widget, index);
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [widget.name.cs, this.name] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}
