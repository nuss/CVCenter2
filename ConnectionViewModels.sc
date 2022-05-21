OscConnectionViewModel {
	classvar <all;
	var <oscConnection;
	var <mc; // models and controllers

	*new { |oscConnection|
		^super.newCopyArgs(oscConnection).init;
	}

	init {
		all ?? { all = List[] };
		all.add(this);
		this.initModelAndControllers
	}

	initModelsAndControllers { |modelsControllers|
		if (modelsControllers.notNil) {
			mc = modelsControllers;
		} {
			mc ?? { mc = () };
		};

		mc.oscDisplay ?? { mc.oscDisplay = () };
		mc.oscDisplay.model ?? {
			mc.oscDiplay.mudel = Ref((
				ipField: nil,
				portField: nil,
				nameField: "/my/cmd/name",
				index: 1,
				connectorButVal: 0,
				editEnabled: true
			))
		}
	}
}

MidiConnectionViewModel {
	classvar <all;
	var <midiConnection;
	var <mc; // models and controllers

	*new { |midiConnection|
		^super.newCopyArgs(midiConnection).init;
	}

	init {
		all ?? { all = List[] };
		all.add(this);
		this.initModelAndControllers
	}

	initModelsAndControllers { |modelsControllers|
		if (modelsControllers.notNil) {
			mc = modelsControllers;
		} {
			mc ?? { mc = () };
		};

		mc.midiDisplay ?? { mc.midiDisplay = () };
		mc.midiDisplay.model ?? {
			mc.midiDisplay.model = Ref((
				src: "source",
				chan: "chan",
				ctrl: "ctrl",
				learn: "L"
			))
		}
	}
}