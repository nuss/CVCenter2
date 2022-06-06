OscConnectionViewMC {
	classvar <all;
	var <oscConnection;

	*new { |oscConnection|
		^super.newCopyArgs(oscConnection).init;
	}

	init {
		all ?? { all = List[] };
		all.add(this);
		this.initModels
	}

	initModels { |modelsControllers|
		var mmc = oscConnection.mc;
		if (modelsControllers.notNil) {
			mmc = mmc ++ modelsControllers;
		} {
			Error("No parent OscConnection given for OscConnectionViewMC given.").throw;
		};

		mmc.oscDisplay ?? { mmc.oscDisplay = () };
		mmc.oscDisplay.model ?? {
			mmc.oscDiplay.mudel = Ref((
				ipField: nil,
				portField: nil,
				nameField: "/my/cmd/name",
				index: 1,
				connectorButVal: 0,
				editEnabled: true
			))
		};


	}
}

MidiConnectionViewMC {
	classvar <all;
	var <midiConnection;

	*new { |midiConnection|
		^super.newCopyArgs(midiConnection).init;
	}

	init {
		all ?? { all = List[] };
		all.add(this);
		this.initModels
	}

	initModels { |modelsControllers|
		var mmc = midiConnection.mc;
		if (modelsControllers.notNil) {
			mmc = mmc ++ modelsControllers;
		} {
			Error("No parent MidiConnection given for MidiConnectionViewMC given.").throw;
		};

		mmc.midiDisplay ?? { mmc.midiDisplay = () };
		mmc.midiDisplay.model ?? {
			mmc.midiDisplay.model = Ref((
				src: "source",
				chan: "chan",
				ctrl: "ctrl",
				learn: "L"
			))
		}
	}

}