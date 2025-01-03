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
			wmc.midiOptions.model = List[];
		};
		wmc.midiOptions.model.add(Ref((
			midiMode: CVWidget.midiMode,
			midiZero: CVWidget.midiZero,
			ctrlButtonGroup: CVWidget.ctrlButtonGroup,
			midiResolution: CVWidget.midiResolution,
			snapDistance: CVWidget.snapDistance
		)));

		wmc.midiConnections ?? { wmc.midiConnections = () };
		wmc.midiConnections.model ?? {
			wmc.midiConnections.model = List[];
		};
		wmc.midiConnections.model.add(Ref(nil));

		wmc.midiDisplay ?? { wmc.midiDisplay = () };
		wmc.midiDisplay.model ?? {
			wmc.midiDisplay.model = List[];
		};
		wmc.midiDisplay.model.add(Ref((
			src: "source...",
			chan: "chan",
			ctrl: "ctrl",
			learn: "L"
		)));
		wmc.midiConnectorNames ?? { wmc.midiConnectorNames = () };
		wmc.midiConnectorNames.model ?? {
			wmc.midiConnectorNames.model = Ref(List[]);
		};
		wmc.midiConnectorNames.model.value_(
			wmc.midiConnectorNames.model.value.add(name);
		);
		// WIP
		wmc.midiInputRange ?? { wmc.midiInputRange = () };
		wmc.midiMappingConstrainters ?? { wmc.midiMappingConstrainters = () };
		wmc.midiMappingConstrainters.name ?? {
			wmc.midiMappingConstrainters.put(name, (lo: CV([-inf, inf].asSpec), hi: CV([-inf, inf].asSpec)))
		};
		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitMidiOptions,
			prInitMidiConnection,
			prInitMidiDisplay,
			prInitMidiConnectorNames
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

		mc.midiConnections.controller ?? {
			mc.midiConnections.controller = SimpleController(mc.midiConnections.model);
		};
		mc.midiConnections.controller.put(\default, { |changer, what ... moreArgs|
			// var index = widget.midiConnectors.indexOf(this);
			var index = moreArgs[0];

			if (changer[index].value.class == Event) {
				slotChanger = changer[index].value;
				// connect
				ccAction = { |val, num, chan, src|
					// only data structure to hold connections is the model
					// we must infer the connections parameters here
					// if (mc.midiConnections.model[index].value.notNil) {
					mc.midiConnections.model[index].value_((num: num, chan: chan, src: src));
					// };
					widget.midiConnectors.indexOf(this) !? {
						this.getMidiMode.switch(
							//  0-127
							0, {
								if ((this.getSnapDistance <= 0).or(
									val/127 < (cv.input + (this.getSnapDistance/2)) and: {
										val/127 > (cv.input - (this.getSnapDistance/2))
								})) {
									cv.input_(val/127);
									[val, cv.input, cv.value].postln;
								};
							},
							// +/-
							1, {
								cv.input_(cv.input + (val-this.getMidiZero/127*this.getMidiResolution));
								[val, cv.input, cv.value].postln;
							}
						)
					}
				};
				makeCCconnection = { |argSrc, argChan, argNum|
					if (allMidiFuncs[widget][index].isNil or: {
						allMidiFuncs[widget][index].func.isNil
					}) {
						allMidiFuncs[widget][index] = MIDIFunc.cc(ccAction, argNum, argChan, argSrc);
					};
					mc.midiDisplay.model[index].value_((
						learn: "X",
						src: mc.midiConnections.model[index].value.src ? "source...",
						chan: mc.midiConnections.model[index].value.chan ? "chan",
						ctrl: mc.midiConnections.model[index].value.num ? "ctrl"
					));
					mc.midiDisplay.model.changedKeys(widget.syncKeys);
					allMidiFuncs[widget][index];
				};

				if (slotChanger.isEmpty) {
					"allMidiFuncs[widget][%] should learn".format(index).inform;
					makeCCconnection.().learn;
				} {
					"allMidiFuncs[widget][%] was set to src: %, channel: %, number: %".format(
						index, slotChanger.src, slotChanger.chan, slotChanger.num
					).inform;
					makeCCconnection.(slotChanger.src, slotChanger.chan, slotChanger.num);
				};
			} {
				// "disconnect - mc.midiDisplay.controller: %".format(mc.midiDisplay.controller).postln;
				mc.midiDisplay.model[index].value_((learn: "L", src: "source...", chan: "chan", ctrl: "ctrl"));
				mc.midiDisplay.model.changedKeys(widget.syncKeys);
				allMidiFuncs[widget][index].clear;
			};
		})
	}

	prInitMidiDisplay { |mc, cv|
		mc.midiDisplay.controller ?? {
			mc.midiDisplay.controller = SimpleController(mc.midiDisplay.model);
		};
		mc.midiDisplay.controller.put(\default, { |changer, what ... moreArgs|
			var index = widget.midiConnectors.indexOf(this);
			"midiDisplay.controller - changer.value: %, moreArgs: %".format(changer.value, moreArgs).postln;
			// ...
		})
	}

	prInitMidiConnectorNames { |mc, cv|
		mc.midiConnectorNames.controller ?? {
			mc.midiConnectorNames.controller = SimpleController(mc.midiConnectorNames.model);
		};
	}

	name {
		var conID = widget.midiConnectors.indexOf(this);
		^widget.wmc.midiConnectorNames.model.value[conID];
	}

	name_ { |name|
		var conID = widget.midiConnectors.indexOf(this);
		var names = widget.wmc.midiConnectorNames.model.value;
		names[conID] = name;
		// for some reason we need to pass the connector ID explicitely here
		// it doesn't seem to be necessary in other cases
		widget.wmc.midiConnectorNames.model.value_(names).changedKeys(widget.syncKeys, conID);
	}

	setMidiMode { |mode|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};

		mc.midiOptions.model[index].value_((
			midiMode: mode,
			midiZero: mc.midiOptions.model[index].value.midiZero,
			ctrlButtonGroup: mc.midiOptions.model[index].value.ctrlButtonGroup,
			midiResolution: mc.midiOptions.model[index].value.midiResolution,
			snapDistance: mc.midiOptions.model[index].value.snapDistance
		));
		mc.midiOptions.model.changedKeys(widget.syncKeys, index);
	}

	getMidiMode {
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		^mc.midiOptions.model[index].value.midiMode;
	}

	setMidiZero { |zeroval|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		zeroval = zeroval.asInteger;

		mc.midiOptions.model[index].value_((
			midiMode: mc.midiOptions.model[index].value.midiMode,
			midiZero: zeroval,
			ctrlButtonGroup: mc.midiOptions.model[index].value.ctrlButtonGroup,
			midiResolution: mc.midiOptions.model[index].value.midiResolution,
			snapDistance: mc.midiOptions.model[index].value.snapDistance
		));
		mc.midiOptions.model.changedKeys(widget.syncKeys, index);
	}

	getMidiZero {
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		^mc.midiOptions.model[index].value.midiZero;
	}

	setSnapDistance { |snapDistance|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		snapDistance = snapDistance.asFloat;

		mc.midiOptions.model[index].value_((
			midiMode: mc.midiOptions.model[index].value.midiMode,
			midiZero: mc.midiOptions.model[index].value.midiZero,
			ctrlButtonGroup: mc.midiOptions.model[index].value.ctrlButtonGroup,
			midiResolution: mc.midiOptions.model[index].value.midiResolution,
			snapDistance: snapDistance
		));
		mc.midiOptions.model.changedKeys(widget.syncKeys, index);
	}

	getSnapDistance {
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		^mc.midiOptions.model[index].value.snapDistance;
	}

	setCtrlButtonGroup { |numButtons|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		if (numButtons.notNil and:{ numButtons.isInteger.not }) {
			Error("setCtrlButtonGroup: 'numButtons' must either be an Integer or nil!").throw;
		};

		mc.midiOptions.model[index].value_((
			midiMode: mc.midiOptions.model[index].value.midiMode,
			midiZero: mc.midiOptions.model[index].value.midiZero,
			ctrlButtonGroup: numButtons,
			midiResolution: mc.midiOptions.model[index].value.midiResolution,
			snapDistance: mc.midiOptions.model[index].value.snapDistance
		));
		mc.midiOptions.model.changedKeys(widget.syncKeys, index);
	}

	getCtrlButtonGroup {
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		^mc.midiOptions.model[index].value.ctrlButtonGroup;
	}

	setMidiResolution { |resolution|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);
		mc.midiOptions.model[index].value_((
			midiMode: mc.midiOptions.model[index].value.midiMode,
			midiZero: mc.midiOptions.model[index].value.midiZero,
			ctrlButtonGroup: mc.midiOptions.model[index].value.ctrlButtonGroup,
			midiResolution: resolution,
			snapDistance: mc.midiOptions.model[index].value.snapDistance
		));
		mc.midiOptions.model.changedKeys(widget.syncKeys, index);
	}

	getMidiResolution {
		var index = widget.midiConnectors.indexOf(this);
		^widget.wmc.midiOptions.model[index].value.midiResolution;
	}

	midiConnect { |src, chan, num|
		var mc = widget.wmc;
		var index = widget.midiConnectors.indexOf(this);

		mc.midiConnections.model[index].value_(
			(src: src, chan: chan, num: num)
		);
		mc.midiConnections.model.changedKeys(widget.syncKeys, index);
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
		// "before: %".format(mc.midiConnections.model[index]).postln;
		mc.midiConnections.model[index].value_(nil);
		// "after: %".format(mc.midiConnections.model[index]).postln;
		mc.midiConnections.model.changedKeys(widget.syncKeys, index);
	}

	remove {
		var index = widget.midiConnectors.indexOf(this);
		var names;
		this.midiDisconnect;
		// "index after disconnect: %".format(index).postln;
		allMidiFuncs[widget][index].free;
		allMidiFuncs[widget].removeAt(index);
		[
			widget.wmc.midiOptions.model,
			widget.wmc.midiConnections.model,
			widget.wmc.midiDisplay.model
		].do(_.removeAt(index));
		// remove name first, otherwise name(s) in select will be incorrect
		names = widget.wmc.midiConnectorNames.model.value;
		names.removeAt(index);
		widget.midiConnectors.remove(this);
		widget.midiConnectors.changed(\value);
		// order matters - next block must be executed
		// after midiConnectors have been changed
		// make sure display in all MIDI editors get set to valid entries
		// MidiConnectorsEditorView is a view which shouldn't necessarily have to exist
		\ConnectorElementView.asClass !? {
			\ConnectorElementView.asClass.subclasses.do { |class|
				class.all[widget] !? {
					class.all[widget].do(_.index_(index))
				}
			}
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [widget, this.name] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}
