OscConnectionModel {
	classvar <all;
	var <widget, <>name;
	var <mc; // models and controllers

	*new { |widget, name|
		^super.newCopyArgs(widget, name).init;
	}

	init {
		var anons;
		all ?? { all = List[] };
		all.add(this);
		anons = all.keys.select { |k| ("New OSC Connection [0-9]+").matchRegexp(k) }.size;
		this.name ?? {
			this.name_("New OSC Connection %".format(anons.size + 1));
		};
		this.initModelsAndControllers;
	}

	initModelsAndControllers { |modelsControllers|
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

MidiConnectionModel {
	classvar <all;
	var <widget, <>name;
	var <mc; // models and controllers

	*new { |widget, name|
		^super.newCopyArgs(widget, name).init;
	}

	init {
		var anons;
		all ?? { all = List[] };
		all.add[this];
		anons = all.keys.select { |k| ("New MIDI Connection [0-9]+").matchRegexp(k) }.size;
		this.name ?? {
			this.name_("New MIDI Connection %".format(anons.size + 1));
		};
		this.initModelsAndControllers();
	}

	initModelsAndControllers { |modelsControllers|
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
			mc.midiConnection.model = Red(nil);
		};

		// add model to list of connection
		// models in the instance' model
		widget.wmc.midi.add(mc);
	}
}