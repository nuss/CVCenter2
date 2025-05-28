OscConnector {
	classvar cAnons = 0;
	var <widget, <>name;

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("An OscConnector can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget, name).init;
	}

	init {
		widget.numOscConnectors = widget.numOscConnectors + 1;
		if (this.name.isNil) {
			this.name_("OSC Connection %".format(widget.numOscConnectors).asSymbol);
		} { this.name_(this.name.asSymbol) };
		// add to the widget's oscConnection and automatically update GUI
		widget.oscConnectors.add(this).changed(\value);
		this.initModels(widget.wmc);
	}

	initModels { |wmc|
		wmc.oscCalibration ?? { wmc.oscCalibration = () };
		wmc.oscCalibration.model ?? {
			wmc.oscCalibration.model = List[];
		};
		wmc.oscCalibration.model.add(Ref(CVWidget.oscCalibration));

		wmc.oscInputRange ?? { wmc.oscInputRange = () };
		wmc.oscInputRange.model ?? {
			wmc.oscInputRange.model = List[];
		};
		wmc.oscInputRange.model.add(Ref([0.0001, 0.0001]));

		wmc.oscConnections ?? { wmc.oscConnections = () };
		wmc.oscConnections.model ?? {
			wmc.oscConnections.model = List[];
		};
		wmc.oscConnections.model.add(Ref(false));

		wmc.oscDisplay ?? { wmc.oscDisplay = () };
		wmc.oscDisplay.model ?? {
			wmc.oscDisplay.model = List[];
		};
		wmc.oscDisplay.model.add(Ref((
			ipField: nil,
			portField: nil,
			nameField: "/my/cmd/name",
			index: 1,
			connectorButVal: 0,
			editEnabled: true
		)));

		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitOscCalibration,
			prInitOscInputRange,
			prInitOscConnection,
			prInitOscDisplay
		].do { |method|
			this.perform(method, wmc, widget.cv)
		}
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
			mc.oscDisplay.controller = SimpleController(mc.oscDisplay.model);
		};
		mc.oscDisplay.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
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
	var <widget;

	*initClass {
		allMidiFuncs = ();
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
		widget.midiConnectors.add(this).changed(\value);

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
			src: "source...",
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
		// WIP
		// wmc.midiInputRange ?? { wmc.midiInputRange = () };
		wmc.midiMappingConstrainters ?? { wmc.midiMappingConstrainters = () };
		wmc.midiMappingConstrainters.name ?? {
			wmc.midiMappingConstrainters.put(name, (lo: CV([-inf, inf].asSpec), hi: CV([-inf, inf].asSpec)))
		};
		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
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
	prInitMidiOptions { |mc, cv|
		mc.midiOptions.controller ?? {
			mc.midiOptions.controller = SimpleController(mc.midiOptions.model);
		};
		mc.midiOptions.controller.put(\default, { |changer, what ... moreArgs|
			var index = widget.midiConnectors.indexOf(this);
		})
	}

	prInitMidiConnection { |mc, cv|
		var ccAction, makeCCconnection;
		var slotChanger;
		var updateModelsFunc = { |num, chan, src, index|
			mc.midiConnections.model.value[index] = (num: num, chan: chan, src: src);
			mc.midiDisplay.model.value[index] = (
				learn: "X",
				src: src ? "source...",
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
			var self = widget.midiConnectors[index];
			var inputMapping, input;

			if (changer[index].value.class == Event) {
				slotChanger = changer[index].value;
				// midiConnect
				updateModelsFunc.(slotChanger.num, slotChanger.chan, slotChanger.src, index);
				ccAction = { |val, num, chan, src|
					// MIDI learn
					// we must infer the connections parameters here
					if (mc.midiConnections.model.value[index].isEmpty) { updateModelsFunc.(num, chan, src, index) };
					// widget.midiConnectors.indexOf(this) !? {
					// "my midiConnector's index: %, this: %, index: %".format(widget.midiConnectors.indexOf(this), this, index).postln;
					inputMapping = self.getMidiInputMapping;
					// "input mapping: %".format(inputMapping).postln;
					self.getMidiMode.switch(
						//  0-127
						0, {
							"midiMode is 0-127".postln;
							input = val/127;
							if ((self.getSnapDistance <= 0).or(
								input < (cv.input + (self.getSnapDistance/2)) and: {
									input > (cv.input - (self.getSnapDistance/2))
							})) {
								case
								{ inputMapping.mapping === \lincurve } {
									cv.input_(input.lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve.postln).postln)
								}
								{ inputMapping.mapping === \linbicurve } {
									cv.input_(input.linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve.postln).postln)
								}
								{ inputMapping.mapping === \linenv } {
									cv.input_(input.linenv(env: inputMapping.env))
								}
								{ inputMapping.mapping === \explin } {
									cv.input_((input+1).explin(1, 2, 0, 1))
								}
								{ inputMapping.mapping === \expexp or: {inputMapping.mapping === \linexp }} {
									if (widget.getSpec.postln.hasZeroCrossing) {
										self.setMidiInputMapping(\linlin);
										cv.input_(input.linlin(0, 1, 0, 1))
									} {
										cv.value_((input+1).perform(inputMapping.mapping, 1, 2, widget.getSpec.minval, widget.getSpec.maxval))
									}
								}
								{
									// "cv.input before: %".format(cv.input).postln;
									cv.input_(input);
									// "cv.input after: %".format(cv.input).postln;
								}
							}
						},
						// +/-
						1, {
							"midiMode is +/-".postln;
							input = cv.input + (val-self.getMidiZero/127*self.getMidiResolution);
							case
							{ inputMapping.mapping === \lincurve } {
								cv.input_(input.lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve.postln).postln)
							}
							{ inputMapping.mapping === \linbicurve } {
								cv.input_(input.linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve.postln).postln)
							}
							{ inputMapping.mapping === \linenv } {
								cv.input_(input.linenv(env: inputMapping.env))
							}
							{ inputMapping.mapping === \explin } {
								cv.input_((input+1).explin(1, 2, 0, 1))
							}
							{ inputMapping.mapping === \expexp or: {inputMapping.mapping === \linexp }} {
								if (widget.getSpec.hasZeroCrossing) {
									self.setMidiInputMapping(\linlin);
									cv.input_(input.linlin(0, 1, 0, 1))
								} {
									cv.value_((input+1).expexp(1, 2, widget.getSpec.minval, widget.getSpec.maxval))
								}
							}
							{ cv.input_(input.linlin(0, 1, 0, 1)) };
						}
					);
					[val/127, cv.input, cv.value].postln;
					// }
				};
				// "widget: %, index: %".format(widget, index).postln;
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
					src: "source...",
					chan: "chan",
					ctrl: "ctrl",
					toolTip: "Click and move hardware slider/knob to connect to"
				);
				mc.midiDisplay.model.changedPerformKeys(widget.syncKeys, index);
				allMidiFuncs[widget][index].clear;
			};
		})
	}

	prInitMidiInputMappings { |mc, cv|
		mc.midiInputMappings.controller ?? {
			mc.midiInputMappings.controller = SimpleController(mc.midiInputMappings.model);
		};
		mc.midiInputMappings.controller.put(\default, { |changer, what ... moreArgs|
			"yadda yadda: %, %, %".format(changer.value, what, moreArgs).postln;
		})
	}

	prInitMidiDisplay { |mc, cv|
		mc.midiDisplay.controller ?? {
			mc.midiDisplay.controller = SimpleController(mc.midiDisplay.model);
		};
		// mc.midiDisplay.controller.put(\default, { |changer, what ... moreArgs|
		// "midiDisplay.controller.triggered".postln;
		// 	var index = widget.midiConnectors.indexOf(this);
		// 	// "midiDisplay.controller - changer.value: %, moreArgs: %".format(changer.value, index).postln;
		// 	// ...
// })
	}

	prInitMidiConnectorNames { |mc, cv|
		mc.midiConnectorNames.controller ?? {
			mc.midiConnectorNames.controller = SimpleController(mc.midiConnectorNames.model);
		};
		// mc.midiConnectorNames.controller.put(\default, { |changer, what ... moreArgs|
		// 	"midiConnectorNames.controller triggered:\n\t%\n\t%\n\t%".format(changer.value, what, moreArgs).postln;
		// })
	}

	name {
		var conID = widget.midiConnectors.indexOf(this);
		^widget.wmc.midiConnectorNames.model.value[conID];
	}

	name_ { |name|
		var conID = widget.midiConnectors.indexOf(this);
		widget.wmc.midiConnectorNames.model.value[conID] = name.asSymbol;
		widget.wmc.midiConnectorNames.model.changedPerformKeys(widget.syncKeys, conID);
	}

	setMidiMode { |mode|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};
		mc.midiOptions.model.value[index].midiMode = mode;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiMode {
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		^mc.midiOptions.model.value[index].midiMode;
	}

	setMidiZero { |zeroval|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		zeroval = zeroval.asInteger;
		mc.midiOptions.model.value[index].midiZero = zeroval;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiZero {
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		^mc.midiOptions.model.value[index].midiZero;
	}

	setSnapDistance { |snapDistance|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		snapDistance = snapDistance.asFloat;
		mc.midiOptions.model.value[index].snapDistance = snapDistance;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getSnapDistance {
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		^mc.midiOptions.model.value[index].snapDistance;
	}

	setCtrlButtonGroup { |numButtons|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		if (numButtons.notNil and:{ numButtons.isInteger.not }) {
			Error("setCtrlButtonGroup: 'numButtons' must either be an Integer or nil!").throw;
		};
		mc.midiOptions.model.value[index].ctrlButtonGroup = numButtons;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getCtrlButtonGroup {
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		^mc.midiOptions.model.value[index].ctrlButtonGroup;
	}

	setMidiResolution { |resolution|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		mc.midiOptions.model.value[index].midiResolution = resolution;
		mc.midiOptions.model.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiResolution {
		var index = widget.midiConnectors.indexOf(this);
		^widget.wmc.midiOptions.model.value[index].midiResolution;
	}

	setMidiInputMapping { |mapping, curve = 0, env(Env([0, 1], [1]))|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
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
		{ mapping === \linexp } {
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
		var index = widget.midiConnectors.indexOf(this);
		^widget.wmc.midiInputMappings.model.value[index];
	}

	midiConnect { |src, chan, num|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
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
		var index = widget.midiConnectors.indexOf(this);
		mc.midiConnections.model.value[index] = nil;
		mc.midiConnections.model.changedPerformKeys(widget.syncKeys, index);
	}

	remove { |forceAll = false|
		var index = widget.midiConnectors.indexOf(this);
		var names, allMS;

		if (widget.midiConnectors.size > 1 or: { forceAll }) {
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
			widget.midiConnectors.remove(this);
			widget.midiConnectors.changed(\value);
			// order matters - next block must be executed
			// after midiConnectors have been changed
			// make sure display in all MIDI editors get set to valid entries
			// MidiConnectorsEditorView is a view which shouldn't necessarily have to exist
			\ConnectorElementView.asClass !? {
				\ConnectorElementView.asClass.subclasses.do { |class|
					// elements that have a meaning in the context of a connector
					// hold an Event in their 'all'' classvar
					// 'global' elements like MidiInitButton hold a List in 'all'
					// following block only needs to run for elements that keep a reference
					// to an index of one or more connectors
					if (class.all.class == Event) {
						class.all[widget] !? {
							if (index > 0) {
								class.all[widget].do(_.index_(index - 1))
							} {
								class.all[widget].do(_.index_(index))
							}
						}
					}
				}
			};
			\MappingSelect.asClass !? {
				\MappingSelect.asClass.all[widget] !? {
					allMS = \MappingSelect.asClass.all[widget];
					if (index > 0) {
						allMS.do(_.index_(index - 1))
					} {
						allMS.do(_.index_(index))
					}
				}
			}
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [widget.name, this.name] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}
