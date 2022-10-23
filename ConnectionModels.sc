OscConnection {
	classvar cAnons = 0;
	var <widget, <>name;
	var <mc; // models and controllers

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("An OscConnection can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget, name).init;
	}

	init {
		this.name ?? {
			widget.numOscConnections = widget.numOscConnections + 1;
			this.name_("OSC Connection %".format(widget.numOscConnections).asSymbol);
		};
		// add to the widget's oscConnection and automatically update GUI
		widget.oscConnections.add(this).changed(\value);
		this.initModels;
	}

	initModels { |modelsControllers|
		if (modelsControllers.notNil) {
			mc = modelsControllers;
		} {
			mc ?? { mc = () };
		};

		mc.oscCalibration ?? { mc.oscCalibration = () };
		mc.oscCalibration.model ?? {
			mc.oscCalibration.model = Ref(CVWidget.oscCalibration);
		};

		mc.oscInputRange ?? { mc.oscInputRange = () };
		mc.oscInputRange.model ?? {
			mc.oscInputRange.model = Ref([0.0001, 0.0001])
		};

		mc.oscConnection ?? { mc.oscConnection = () };
		mc.oscConnection.model ?? {
			mc.oscConnection.model = Ref(false);
		};

		mc.oscDisplay ?? { mc.oscDisplay = () };
		mc.oscDisplay.model ?? {
			mc.oscDisplay.model = Ref((
				ipField: nil,
				portField: nil,
				nameField: "/my/cmd/name",
				index: 1,
				connectorButVal: 0,
				editEnabled: true
			))
		};

		// add model to list of connection
		// models in the instance' model
		widget.wmc.osc.add(mc);

		this.initControllers;
	}

	initControllers {
		#[
			prInitOscCalibration,
			prInitOscInputRange,
			prInitOscConnect,
			prInitOscDisplay
		].do { |method|
			this.perform(method, mc, widget.cv)
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

	prInitOscConnect { |mc, cv|
		mc.oscConnection.controller ?? {
			mc.oscConnection.controller = SimpleController(mc.oscConnection.model)
		};
		mc.oscConnection.controller.put(\default, { |changer, what, moreArgs|
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
		^nil;
	}

	setMidiMode { |mode|

	}
}

MidiConnection {
	classvar cAnons = 0;
	var <widget, <>name;
	var <mc; // models and controllers

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("A MidiConnection can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget, name).init;
	}

	init {
		this.name ?? {
			widget.numMidiConnections = widget.numMidiConnections + 1;
			this.name_("MIDI Connection %".format(widget.numMidiConnections).asSymbol);
		};
		widget.midiConnections.add(this).changed(\value);
		this.initModels;
	}

	initModels { |modelsControllers|
		if (modelsControllers.notNil) {
			mc = modelsControllers;
		} {
			mc ?? { mc = () };
		};

		mc.midiOptions ?? { mc.midiOptions = () };
		mc.midiOptions.model ?? {
			mc.midiOptions.model = Ref((
				midiMode: CVWidget.midiMode,
				midiMean: CVWidget.midiMean,
				ctrlButtonBank: CVWidget.ctrlButtonBank,
				midiResolution: CVWidget.midiResolution,
				softWithin: CVWidget.softWithin
			))
		};

		mc.midiConnection ?? { mc.midiConnection = () };
		mc.midiConnection.model ?? {
			mc.midiConnection.model = Ref(nil);
		};

		mc.midiDisplay ?? { mc.midiDisplay = () };
		mc.midiDisplay.model ?? {
			mc.midiDisplay.model = Ref((
				src: "source",
				chan: "chan",
				ctrl: "ctrl",
				learn: "L"
			))
		};

		// add model to list of connection
		// models in the instance' model
		widget.wmc.midi.add(mc);

		this.initControllers
	}

	initControllers {
		#[
			prInitMidiOptions,
			prInitMidiConnect,
			prInitMidiDisplay
		].do { |method|
			this.perform(method, mc, widget.cv)
		}
	}

	// private: default controllers
	prInitMidiOptions {
		mc.midiOptions.controller ?? {
			mc.midiOptions.controller = SimpleController(mc.midiOptions.model);
		};
		mc.midiOptions.controller.put(\default, { |changer, what, moreArgs|
			// ...
		})
	}

	prInitMidiConnect {
		mc.midiConnection.controller ?? {
			mc.midiConnection.controller = SimpleController(mc.midiConnection.model);
		};
		mc.midiConnection.controller.put(\default, { |changer, what, moreArgs|
			// ...
		})
	}

	prInitMidiDisplay {
		mc.midiDisplay.controller ?? {
			mc.midiDisplay.controller = SimpleController(mc.midiDisplay.model);
		};
		mc.midiDisplay.controller.put(\default, { |changer, what, moreArgs|
			// ...
		})
	}

	setMidiMode { |mode|
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};

		mc.optionsModel.value_((
			midiMode: mode,
			midiMean: mc.optionsModel.midiMean,
			ctrlButtonBank: mc.optionsModel.ctrlButtonBank,
			midiResolution: mc.optionsModel.midiResolution,
			softWithin: mc.optionsModel.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiMode {
		^mc.midiOptions.model.value.midiMode;
	}

	setMidiMean { |meanval|
		meanval = meanval.asInteger;

		mc.optionsModel.value_((
			midiMode: mc.optionsModel.midiMode,
			midiMean: meanval,
			ctrlButtonBank: mc.optionsModel.ctrlButtonBank,
			midiResolution: mc.optionsModel.midiResolution,
			softWithin: mc.optionsModel.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiMean {
		^mc.midiOptions.value.midiMean;
	}

	setSoftWithin { |threshold|
		threshold = threshold.asFloat;

		mc.optionsModel.value_((
			midiMode: mc.optionsModel.midiMode,
			midiMean: mc.optionsModel.midiMean,
			ctrlButtonBank: mc.optionsModel.ctrlButtonBank,
			midiResolution: mc.optionsModel.midiResolution,
			softWithin: threshold
		)).changedKeys(widget.syncKeys);
	}

	getSoftWithin {
		^mc.midiOptions.value.softWithin;
	}

	setCtrlButtonBank { |numSliders|
		if (numSliders.notNil and:{ numSliders.isInteger.not }) {
			Error("setCtrlButtonBank: 'numSliders' must either be an Integer or nil!").throw;
		};

		mc.optionsModel.value_((
			midiMode: mc.optionsModel.midiMode,
			midiMean: mc.optionsModel.midiMean,
			ctrlButtonBank: numSliders,
			midiResolution: mc.optionsModel.midiResolution,
			softWithin: mc.optionsModel.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getCtrlButtonBank {
		^mc.midiOptions.value.ctrlButtonBank;
	}

	setMidiResolution { |resolution|
		mc.optionsModel.value_((
			midiMode: mc.optionsModel.midiMode,
			midiMean: mc.optionsModel.midiMean,
			ctrlButtonBank: mc.optionsModel.ctrlButtonBank,
			midiResolution: mc.resolution,
			softWithin: mc.optionsModel.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiResolution {
		^mc.midiOptions.value.midiResolution;
	}

	midiConnect { |uid, chan, num|
		// any further checks
		mc.midiConnection.model.value_(
			(src: uid, chan: chan, num: num)
		).changedKeys(widget.syncKeys);
		// TODO - check settings system
		CmdPeriod.add({
			if (this.class.removeResponders) {
				this !? { this.midiDisconnect }
			}
		})
	}

	midiDisconnect {
		mc.midiConnection.model.value_(nil).changedKeys(widget.syncKeys);
	}

	remove {
		// remove views, MIDIdefs...
		^nil;
	}

}
