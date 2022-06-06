OscConnection {
	classvar <all;
	var <widget, <>name;
	var <mc; // models and controllers
	var cAnons = 0;

	*new { |widget, name|
		^super.newCopyArgs(widget, name).init;
	}

	init {
		all ?? { all = List[] };
		all.add(this);
		this.name ?? {
			cAnons = cAnons + 1;
			this.name_("New OSC Connection %".format(cAnons));
		};
		this.initModelsAndControllers;
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
	}
}

MidiConnection {
	classvar <all;
	var <widget, <>name;
	var <mc; // models and controllers
	var cAnons = 0;

	*new { |widget, name|
		^super.newCopyArgs(widget, name).init;
	}

	init {
		var anons;
		all ?? { all = List[] };
		all.add[this];
		this.name ?? {
			cAnons = cAnons + 1;
			this.name_("New MIDI Connection %".format(cAnons));
		};
		this.initModelsAndControllers();
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
	}

	setMidiMode { |mode|
		var optionsModel = widget.wmc.midi.midiOptions.model;

		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};

		optionsModel.value_((
			midiMode: mode,
			midiMean: optionsModel.midiMean,
			ctrlButtonBank: optionsModel.ctrlButtonBank,
			midiResolution: optionsModel.midiResolution,
			softWithin: optionsModel.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiMode {
		^widget.wmc.midi.midiOptions.model.value.midiMode;
	}

	setMidiMean { |meanval|
		var optionsModel = widget.wmc.midi.midiOptions.model;

		meanval = meanval.asInteger;

		optionsModel.value_((
			midiMode: optionsModel.midiMode,
			midiMean: meanval,
			ctrlButtonBank: optionsModel.ctrlButtonBank,
			midiResolution: optionsModel.midiResolution,
			softWithin: optionsModel.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiMean {
		^widget.wmc.midi.midiOptions.value.midiMean;
	}

	setSoftWithin { |threshold|
		var optionsModel = widget.wmc.midi.midiOptions.model;

		threshold = threshold.asFloat;

		optionsModel.value_((
			midiMode: optionsModel.midiMode,
			midiMean: optionsModel.midiMean,
			ctrlButtonBank: optionsModel.ctrlButtonBank,
			midiResolution: optionsModel.midiResolution,
			softWithin: threshold
		)).changedKeys(widget.syncKeys);
	}

	getSoftWithin {
		^widget.wmc.midi.midiOptions.value.softWithin;
	}

	setCtrlButtonBank { |numSliders|
		var optionsModel = widget.wmc.midi.midiOptions.model;

		if (numSliders.notNil and:{ numSliders.isInteger.not }) {
			Error("setCtrlButtonBank: 'numSliders' must either be an Integer or nil!").throw;
		};

		optionsModel.value_((
			midiMode: optionsModel.midiMode,
			midiMean: optionsModel.midiMean,
			ctrlButtonBank: numSliders,
			midiResolution: optionsModel.midiResolution,
			softWithin: optionsModel.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getCtrlButtonBank {
		^widget.wmc.midi.midiOptions.value.ctrlButtonBank;
	}

	setMidiResolution { |resolution|
		var optionsModel = widget.wmc.midi.midiOptions.model;

		optionsModel.value_((
			midiMode: optionsModel.midiMode,
			midiMean: optionsModel.midiMean,
			ctrlButtonBank: optionsModel.ctrlButtonBank,
			midiResolution: resolution,
			softWithin: optionsModel.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiResolution {
		^widget.wmc.midi.midiOptions.value.midiResolution;
	}
}