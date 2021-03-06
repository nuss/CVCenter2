OscConnection {
	classvar <all, cAnons = 0;
	var <widget, <>name;
	var <mc; // models and controllers

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(AbstractCVWidget).not
		}) {
			Error("An OscConnection can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget, name).init;
	}

	init {
		all ?? { all = List[] };
		all.add(this);
		this.name ?? {
			cAnons = cAnons + 1;
			this.name_("OSC Connection %".format(cAnons).asSymbol);
		};
		widget.oscConnections.put(this.name, this);
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
			mc.oscCalibration.model = Ref(AbstractCVWidget.oscCalibration);
		};

		mc.oscInputRange ?? { mc.oscInputRange = () };
		mc.oscInputRange.model ?? {
			mc.oscInputRange.model = Ref([0.0001, 0.0001])
		};

		mc.oscConnection ?? { mc.oscConnection = () };
		mc.oscConnection.model ?? {
			mc.oscConnection.model = Ref(false);
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
			prInitOscConnect
		].do { |method|
			this.perform(method, mc, widget.cv)
		}
	}

	prInitOscCalibration {}
	prInitOscInputRange {}
	prInitOscConnect {}
}

MidiConnection {
	classvar <all, cAnons = 0;
	var <widget, <>name;
	var <mc; // models and controllers

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(AbstractCVWidget).not
		}) {
			Error("A MidiConnection can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget, name).init;
	}

	init {
		all ?? { all = List[] };
		all.add(this);
		this.name ?? {
			cAnons = cAnons + 1;
			this.name_("MIDI Connection %".format(cAnons).asSymbol);
		};
		widget.midiConnections.put(this.name, this);
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
				midiMode: AbstractCVWidget.midiMode,
				midiMean: AbstractCVWidget.midiMean,
				ctrlButtonBank: AbstractCVWidget.ctrlButtonBank,
				midiResolution: AbstractCVWidget.midiResolution,
				softWithin: AbstractCVWidget.softWithin
			))
		};

		mc.midiConnection ?? { mc.midiConnection = () };
		mc.midiConnection.model ?? {
			mc.midiConnection.model = Ref(nil);
		};

		// add model to list of connection
		// models in the instance' model
		widget.wmc.midi.add(mc);

		this.initControllers
	}

	initControllers {
		#[
			prInitMidiOptions,
			prInitMidiConnect
		].do { |method|
			this.perform(method, mc, widget.cv)
		}
	}

	// private: default controllers
	prInitMidiOptions {}

	prInitMidiConnect {
		mc.midiConnection.controller ?? {
			mc.midiConnection.controller = SimpleController(mc.midiConnection.model);
		};
		mc.midiConnection.controller.put(\default, { |changer, what, moreArgs|

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

}