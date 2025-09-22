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
		widget.wmc.oscConnectors.m.value_(
			widget.wmc.oscConnectors.m.value.add(this)
		).changedPerformKeys(widget.syncKeys);
	}

	initModels { |wmc, name|
		wmc.oscCalibration ?? { wmc.oscCalibration = () };
		wmc.oscCalibration.m ?? {
			wmc.oscCalibration.m = Ref(List[]);
		};
		wmc.oscCalibration.m.value.add(CVWidget.oscCalibration);

		wmc.oscInputRange ?? { wmc.oscInputRange = () };
		wmc.oscInputRange.m ?? {
			wmc.oscInputRange.m = Ref(List[]);
		};
		wmc.oscInputRange.m.value.add([0.0001, 0.0001]);

		wmc.oscConnections ?? { wmc.oscConnections = () };
		wmc.oscConnections.m ?? {
			wmc.oscConnections.m = Ref(List[]);
		};
		wmc.oscConnections.m.value.add(false);

		wmc.oscDisplay ?? { wmc.oscDisplay = () };
		wmc.oscDisplay.m ?? {
			wmc.oscDisplay.m = Ref(List[]);
		};
		wmc.oscDisplay.m.value.add((
			ipField: nil,
			portField: nil,
			// withPort: false,
			nameField: '/my/cmd/name',
			index: 1,
			connectorButVal: 0,
			editEnabled: true
		));

		wmc.oscSelectsStates ?? { wmc.oscSelectsStates = () };
		wmc.oscSelectsStates.m ?? {
			wmc.oscSelectsStates.m = Ref(List[]);
		};
		wmc.oscSelectsStates.m.value.add((
			ipSelect: 0,
			portSelect: 0,
			cmdSelect: 0
		));

		wmc.oscConnectorNames ?? { wmc.oscConnectorNames = () };
		wmc.oscConnectorNames.m ?? {
			wmc.oscConnectorNames.m = Ref(List[]);
		};
		wmc.oscConnectorNames.m.value.add(name);

		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitOscCalibration,
			prInitOscInputRange,
			prInitOscConnection,
			prInitOscDisplay,
			prInitOscSelectsStates,
			prInitOscConnectors,
			prInitOscConnectorNames
		].do { |method|
			this.perform(method, wmc, widget.cv)
		}
	}

	prInitOscConnectors { |mc, cv|
		mc.oscConnectors.c ?? {
			mc.oscConnectors.c = SimpleController(mc.oscConnectors.m)
		};
		mc.oscConnectors.c.put(\default, { |changer, what ... moreArgs|
			"blablabla, do something..."
		})
	}

	prInitOscCalibration { |mc, cv|
		mc.oscCalibration.c ?? {
			mc.oscCalibration.c = SimpleController(mc.oscCalibration.m)
		};
		mc.oscCalibration.c.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscInputRange { |mc, cv|
		mc.oscInputRange.c ?? {
			mc.oscInputRange.c = SimpleController(mc.oscInputRange.m)
		};
		mc.oscInputRange.c.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscConnection { |mc, cv|
		mc.oscConnections.c ?? {
			mc.oscConnections.c = SimpleController(mc.oscConnections.m)
		};
		mc.oscConnections.c.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscDisplay { |mc, cv|
		mc.oscDisplay.c ?? {
			mc.oscDisplay.c = SimpleController(mc.oscDisplay.m)
		};
		mc.oscDisplay.c.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscSelectsStates { |mc, cv|
		mc.oscSelectsStates.c ?? {
			mc.oscSelectsStates.c = SimpleController(mc.oscSelectsStates.m)
		};
		mc.oscSelectsStates.c.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscConnectorNames { |mc, cv|
		mc.oscConnectorNames.c ?? {
			mc.oscConnectorNames.c = SimpleController(mc.oscConnectorNames.m)
		};
		mc.oscConnectorNames.c.put(\default, { |changer, what, moreArgs|

		})
	}

	name {
		var conID = widget.wmc.oscConnectors.m.value.indexOf(this);
		if (conID.notNil) {
			^widget.wmc.oscConnectorNames.m.value[conID]
		} { ^nil };
	}

	name_ { |name|
		var conID = widget.wmc.oscConnectors.m.value.indexOf(this);
		conID !? {
			widget.wmc.oscConnectorNames.m.value[conID] = name.asSymbol;
			widget.wmc.oscConnectorNames.m.changedPerformKeys(widget.syncKeys, conID);
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
		widget.wmc.midiConnectors.m.value_(
			widget.wmc.midiConnectors.m.value.add(this)
		).changedPerformKeys(widget.syncKeys);

		allMidiFuncs[widget] ?? {
			allMidiFuncs.put(widget, List[])
		};
		allMidiFuncs[widget].add(nil);
	}

	initModels { |wmc, name|
		wmc.midiOptions ?? { wmc.midiOptions = () };
		wmc.midiOptions.m ?? {
			wmc.midiOptions.m = Ref(List[]);
		};
		wmc.midiOptions.m.value.add((
			midiMode: CVWidget.midiMode,
			midiZero: CVWidget.midiZero,
			ctrlButtonGroup: CVWidget.ctrlButtonGroup,
			midiResolution: CVWidget.midiResolution,
			snapDistance: CVWidget.snapDistance
		));

		wmc.midiConnections ?? { wmc.midiConnections = () };
		wmc.midiConnections.m ?? {
			wmc.midiConnections.m = Ref(List[]);
		};
		wmc.midiConnections.m.value.add(nil);

		wmc.midiInputMappings ?? { wmc.midiInputMappings = () };
		wmc.midiInputMappings.m ?? {
			wmc.midiInputMappings.m = Ref(List[]);
		};
		wmc.midiInputMappings.m.value.add((mapping: \linlin));

		wmc.midiDisplay ?? { wmc.midiDisplay = () };
		wmc.midiDisplay.m ?? {
			wmc.midiDisplay.m = Ref(List[]);
		};
		wmc.midiDisplay.m.value.add((
			src: 'source...',
			chan: "chan",
			ctrl: "ctrl",
			learn: "L",
			toolTip: "Click and move hardware slider/knob to connect to"
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
			prInitMidiInputMappings,
			prInitMidiConnection,
			prInitMidiDisplay
		].do { |method|
			this.perform(method, wmc, widget.cv)
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
			var index = mc.midiConnectors.m.value.indexOf(this);
		})
	}

	prInitMidiConnection { |mc, cv|
		var ccAction, makeCCconnection;
		var slotChanger;
		var updateModelsFunc = { |num, chan, src, index|
			mc.midiConnections.m.value[index] = (num: num, chan: chan, src: src);
			mc.midiDisplay.m.value[index] = (
				learn: "X",
				src: src ? 'source...',
				chan: chan ? "chan",
				ctrl: num ? "ctrl",
				toolTip: "Click to disconnect"
			);
			mc.midiDisplay.m.changedPerformKeys(widget.syncKeys, index);
		};

		mc.midiConnections.c ?? {
			mc.midiConnections.c = SimpleController(mc.midiConnections.m);
		};
		mc.midiConnections.c.put(\default, { |changer, what ... moreArgs|
			var index = moreArgs[0];
			// brute force fix - why is 'this' not considered correctly?
			var self = mc.midiConnectors.m.value[index];
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
					if (mc.midiConnections.m.value[index].isEmpty) { updateModelsFunc.(num, chan, src, index) };
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
				mc.midiDisplay.m.value[index] = (
					learn: "L",
					src: 'source...',
					chan: "chan",
					ctrl: "ctrl",
					toolTip: "Click and move hardware slider/knob to connect to"
				);
				mc.midiDisplay.m.changedPerformKeys(widget.syncKeys, index);
				allMidiFuncs[widget][index].clear;
				if (mc.midiConnections.m.value.select(_.notNil).isEmpty) {
					// no conections for widget, uninitialize accum[widget]
					accum[widget] !? { accum[widget] = nil };
				}
			};
		})
	}

	prInitMidiInputMappings { |mc, cv|
		mc.midiInputMappings.c ?? {
			mc.midiInputMappings.c = SimpleController(mc.midiInputMappings.m);
		};
		mc.midiInputMappings.c.put(\default, { |changer, what ... moreArgs|
			// "yadda yadda: %, %, %".format(changer.value, what, moreArgs).postln;
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

	name {
		var mc = widget.wmc;
		var conID = mc.midiConnectors.m.value.indexOf(this);
		if (conID.notNil) {
			^widget.wmc.midiConnectorNames.m.value[conID]
		} { ^nil }
	}

	name_ { |name|
		var mc = widget.wmc;
		var conID = mc.midiConnectors.m.value.indexOf(this);
		conID !? {
			widget.wmc.midiConnectorNames.m.value[conID] = name.asSymbol;
			widget.wmc.midiConnectorNames.m.changedPerformKeys(widget.syncKeys, conID);
		}
	}

	setMidiMode { |mode|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};
		mc.midiOptions.m.value[index].midiMode = mode;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiMode {
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		^mc.midiOptions.m.value[index].midiMode;
	}

	setMidiZero { |zeroval|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		zeroval = zeroval.asInteger;
		mc.midiOptions.m.value[index].midiZero = zeroval;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiZero {
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		^mc.midiOptions.m.value[index].midiZero;
	}

	setSnapDistance { |snapDistance|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		snapDistance = snapDistance.asFloat;
		mc.midiOptions.m.value[index].snapDistance = snapDistance;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getSnapDistance {
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		^mc.midiOptions.m.value[index].snapDistance;
	}

	setCtrlButtonGroup { |numButtons|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		if (numButtons.notNil and:{ numButtons.isInteger.not }) {
			Error("setCtrlButtonGroup: 'numButtons' must either be an Integer or nil!").throw;
		};
		mc.midiOptions.m.value[index].ctrlButtonGroup = numButtons;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getCtrlButtonGroup {
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		^mc.midiOptions.m.value[index].ctrlButtonGroup;
	}

	setMidiResolution { |resolution|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		mc.midiOptions.m.value[index].midiResolution = resolution;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiResolution {
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		^widget.wmc.midiOptions.m.value[index].midiResolution;
	}

	setMidiInputMapping { |mapping, curve = 0, env(Env([0, 1], [1]))|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		mapping = mapping.asSymbol;
		[\linlin, \linexp, \explin, \expexp, \lincurve, \linbicurve, \linenv].indexOf(mapping) ?? {
			"arg 'mapping' must be one of \\linlin, \\linexp, \\explin, \\expexp, \\lincurve, \\linbicurve or \\linenv".error;
			^nil
		};
		mc.midiInputMappings.m.value[index].mapping = mapping;
		case
		{ mapping === \lincurve or: { mapping === \linbicurve }} {
			mc.midiInputMappings.m.value[index].curve = curve;
			mc.midiInputMappings.m.value[index].env = nil;
		}
		{ mapping === \linenv } {
			mc.midiInputMappings.m.value[index].curve = nil;
			mc.midiInputMappings.m.value[index].env = env;
		}
		{
			mc.midiInputMappings.m.value[index].curve = nil;
			mc.midiInputMappings.m.value[index].env = nil;
		};
		mc.midiInputMappings.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiInputMapping {
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		^widget.wmc.midiInputMappings.m.value[index];
	}

	midiConnect { |src, chan, num|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		mc.midiConnections.m.value[index] = (src: src, chan: chan, num: num);
		mc.midiConnections.m.changedPerformKeys(widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? {
				allMidiFuncs[widget][index].permanent_(this.widget.class.removeResponders)
			}
		})
	}

	midiDisconnect {
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		mc.midiConnections.m.value[index] = nil;
		mc.midiConnections.m.changedPerformKeys(widget.syncKeys, index);
	}

	remove { |forceAll = false|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		var names, allMS;

		if (mc.midiConnectors.m.value.size > 1 or: { forceAll }) {
			this.midiDisconnect;
			allMidiFuncs[widget][index].free;
			allMidiFuncs[widget].removeAt(index);
			[
				widget.wmc.midiOptions.m.value,
				widget.wmc.midiConnections.m.value,
				widget.wmc.midiDisplay.m.value,
				widget.wmc.midiConnectorNames.m.value,
				widget.wmc.midiInputMappings.m.value
			].do(_.removeAt(index));
			mc.midiConnectors.m.value.remove(this);
			mc.midiConnectors.m.value.changed(\value);
			// set editor elements (and other custom elements depending
			// on mc.midiConnectors.m.value) to suitable connector
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
