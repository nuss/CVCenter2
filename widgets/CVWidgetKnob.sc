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

		wmc.midiOptions ?? { wmc.midiOptions = () };
		wmc.midiOptions.model ?? {
			wmc.midiOptions.model = Ref((
				midiMode: this.class.midiMode,
				midiMean: this.class.midiMean,
				ctrlButtonBank: this.class.ctrlButtonBank,
				midiResolution: this.class.midiResolution,
				softWithin: this.class.softWithin
			))
		};

		wmc.cvGuiConnections ?? { wmc.cvGuiConnections = () };
		wmc.cvGuiConnections.model ?? {
			wmc.cvGuiConnections.model = Ref([true, true])
		};

		/*wmc.widgetDisplay ?? { wmc.widgetDisplay = () };
		wmc.widgetDisplay.model ?? {
			wmc.widgetDisplay.model = Ref((
				// midiSrc: "source",
				// midiChan: "chan",
				// midiCtrl: "ctrl",
				// midiLearn: "L",
				midiButton: ["edit MIDI", AbstractCVWidgetGui.stringColor, AbstractCVWidgetGui.backgroundColor],
				specButton: ["edit Spec", AbstractCVWidgetGui.specsStringColor, AbstractCVWidgetGui.specsBackgroundColor],
				actionButton: ["actions (0/0)", AbstractCVWidgetGui.actionsStringColor, AbstractCVWidgetGui.actionsBackgroundColor]
			))
		};*/

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
		var thisWmcOsc, thisWmcMidi;

		// sanitize 'name'
		name = name.asSymbol;
		wmc.osc[name] ?? {
			wmc.osc.put(name, ());
			thisWmcOsc = wmc.osc[name];

			thisWmcOsc.oscCalibration ?? { thisWmcOsc.oscCalibration = () };
			thisWmcOsc.oscCalibration.model ?? {
				thisWmcOsc.oscCalibration.model = Ref(this.oscCalibration);
			};

			thisWmcOsc.oscInputRange ?? { thisWmcOsc.oscInputRange = () };
			thisWmcOsc.oscInputRange.model ?? {
				thisWmcOsc.oscInputRange.model = Ref([0.0001, 0.0001])
			};

			thisWmcOsc.oscConnection ?? { thisWmcOsc.oscConnection = () };
			thisWmcOsc.oscConnection.model ?? {
				thisWmcOsc.oscConnection.model = Ref(false);
			};

			thisWmcOsc.oscDisplay ?? { thisWmcOsc.oscDisplay = () };
			thisWmcOsc.oscDisplay.model ?? {
				thisWmcOsc.oscDisplay.model = AbstractCVWidgetGui.defaultOscDisplayModel
			}
		};
		wmc.midi[name] ?? {
			wmc.midi.put(name, ());
			thisWmcMidi = wmc.midi[name];

			thisWmcMidi.midiConnection ?? { thisWmcMidi.midiConnection = () };
			thisWmcMidi.midiConnection.model ?? {
				thisWmcMidi.midiConnection.model = Red(nil);
			}
		}
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