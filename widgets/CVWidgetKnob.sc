CVWidgetKnob : AbstractCVWidget {
	var <name, <cv;

	var <wmc; //widget models and controllers
	var <syncKeys, syncedActions;
	var <oscConnections, <midiConnections;

	*new { |name, cv|
		^super.newCopyArgs(name, cv).init/*(setup, action)*/;
	}

	init { |setupArgs, action|
		name ?? {
			Error("No name provided for new CVWidgetKnob").throw;
		};

		cv ?? { cv = CV.new };

		all[name] ?? { all.put(this) };
		#oscConnections, midiConnections = ()!2;

		setupArgs !? {
			setupArgs.isKindOf(Dictionary).not.if {
				Error("a setup has to be provided as a Dictionary or an Event").throw
			};
			this.setMidiMode(setupArgs[\midiMode] ? this.class.midiMode);
			this.setMidiResolution(setupArgs[\midiResolution] ? this.class.midiResolution);
			this.setMidiMean(setupArgs[\midiMean] ? this.class.midiMean);
			this.setCtrlButtonBank(setupArgs[\ctrlButtonBank]);
			this.setSoftWithin(setupArgs[\softWithin] ? this.class.softWithin);
			this.setOscCalibration(setupArgs[\oscCalibration] ? this.class.oscCalibration);
		};

		action !? { this.addAction(\default, action) };

		syncKeys ?? { syncKeys = [\default] };

		this.initModelsAndControllers;
	}

	initModelsAndControllers { |modelsControllers|
		if (modelsControllers.notNil) {
			wmc = modelsControllers
		} {
			wmc ?? { wmc = () }
		};


		wmc.cvSpec ?? { wmc.cvSpec = () };
		wmc.cvSpec.model ?? {
			wmc.cvSpec.model = Ref(this.getSpec);
		};

		wmc.actions ?? { wmc.actions = () };
		wmc.actions.model ?? {
			wmc.actions.model = Ref((numActions: 0, activeActions: 0))
		};

		wmc.cvGuiConnections ?? { wmc.cvGuiConnections = () };
		wmc.cvGuiConnections.model ?? {
			wmc.cvGuiConnections.model = Ref([true, true])
		};

		wmc.widgetDisplay ?? { wmc.widgetDisplay = () };
		wmc.widgetDisplay.model ?? {
			wmc.widgetDisplay.model = Ref((
				oscButton: ["edit OSC", AbstractCVWidgetGui.stringColor, AbstractCVWidget.backgroundColor],
				// midiSrc: "source",
				// midiChan: "chan",
				// midiCtrl: "ctrl",
				// midiLearn: "L",
				midiButton: ["edit MIDI", AbstractCVWidgetGui.stringColor, AbstractCVWidgetGui.backgroundColor],
				specButton: ["edit Spec", AbstractCVWidgetGui.specsStringColor, AbstractCVWidgetGui.specsBackgroundColor],
				actionButton: ["actions (0/0)", AbstractCVWidgetGui.actionsStringColor, AbstractCVWidgetGui.actionsBackgroundColor]
			))
		};

		// we want to allow more than one MIDI/OSC connection...
		// hence, we need name slots for each connection

		/***
		* OSC connections - namespace: \osc
		* expecting a unique name for every connection
		*/
		wmc.osc ?? { wmc.osc = () };
		wmc.midi ?? { wmc.midi = () };

		"wmc: %".format(wmc).postln;
	}

	initOscModelsControllers { |name|
		var thisWmc;

		// sanitize 'name'
		name = name.asSymbol;
		wmc.osc[name] ?? {
			wmc.osc.put(name, ());
			thisWmc = wmc.osc[name];

			thisWmc.oscCalibration ?? { thisWmc.oscCalibration = () };
			thisWmc.oscCalibration.model ?? {
				thisWmc.oscCalibration.model = Ref(this.oscCalibration);
			};

			thisWmc.oscInputRange ?? { thisWmc.oscInputRange = () };
			thisWmc.oscInputRange.model ?? {
				thisWmc.oscInputRange.model = Ref([0.0001, 0.0001])
			};

			thisWmc.oscConnection ?? { thisWmc.oscConnection = () };
			thisWmc.oscConnection.model ?? {
				thisWmc.oscConnection.model = Ref(false);
			};

			thisWmc.oscDisplay ?? { thisWmc.oscDisplay = () };
			thisWmc.oscDisplay.model ?? {
				thisWmc.oscDisplay.model = AbstractCVWidgetGui.defaultOscDisplayModel
			}
		};
	}

	// the CV's ControlSpec
	setSpec {}
	getSpec {}
	// CV actions
	addAction {}
	removeAction {}
	activateAction {}
	// MIDI
	setMidiMode {}
	getMidiMode {}
	setMidiMean {}
	getMidiMean {}
	setSoftWithin {}
	getSoftWithin {}
	setCtrlButtonBank {}
	getCtrlButtonBank {}
	setMidiResolution {}
	getMidiResolution {}
	midiConnect {}
	midiDisconnect {}
	// OSC
	setOscCalibration {}
	getOscCalibration {}
	setOscMapping {}
	getOscMapping {}
	setOscInputConstraints {}
	getOscInputConstraints {}
	oscConnect {}
	oscDisconnect {}

	initControllerActions {}

}