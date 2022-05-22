AbstractCVWidget {
	classvar <all;
	classvar <>removeResponders = true, <>midiSources, <>shortcuts, prefs;
	classvar <>midiMode = 0, <>midiResolution = 1, <>midiMean = 0.1, <>ctrlButtonBank, <>softWithin = 0.1, <>oscCalibration = true;

	*initClass {
		var scPrefs = false;

		Class.initClassTree(KeyDownActions);
		// FIXME: CVWidgetShortcuts
		Class.initClassTree(CVWidgetShortcuts);

		// all CVWidgets
		all = ();

		this.midiSources = ();

		prefs = CVCenterPreferences.readPreferences;
		prefs !? { prefs[\shortcuts] !? { prefs[\shortcuts][\cvwidget] !? { scPrefs = true }}};

		this.shortcuts = IdentityDictionary.new;

		if(scPrefs.not, {
			this.shortcuts = CVWidgetShortcuts.shortcuts;
		}, {
			this.shortcuts = prefs[\shortcuts][\cvwidget];
		})
	}

	*globalSetup {
		^(
			midiMode: this.midiMode,
			midiResolution: this.midiResolution,
			midiMean: this.midiMean,
			ctrlButtonBank: this.ctrlButtonBank,
			softWithin: this.softWithin,
			oscCalibration: this.oscCalibration
		);
	}

	// the CV's ControlSpec
	setSpec { this.subclassResponsibility(thisMethod) }
	getSpec { this.subclassResponsibility(thisMethod) }
	// CV actions
	addAction { this.subclassResponsibility(thisMethod) }
	removeAction { this.subclassResponsibility(thisMethod) }
	activateAction { this.subclassResponsibility(thisMethod) }
	// MIDI
	setMidiMode { this.subclassResponsibility(thisMethod) }
	getMidiMode { this.subclassResponsibility(thisMethod) }
	setMidiMean { this.subclassResponsibility(thisMethod) }
	getMidiMean { this.subclassResponsibility(thisMethod) }
	setSoftWithin { this.subclassResponsibility(thisMethod) }
	getSoftWithin { this.subclassResponsibility(thisMethod) }
	setCtrlButtonBank { this.subclassResponsibility(thisMethod) }
	getCtrlButtonBank { this.subclassResponsibility(thisMethod) }
	setMidiResolution { this.subclassResponsibility(thisMethod) }
	getMidiResolution { this.subclassResponsibility(thisMethod) }
	midiConnect { this.subclassResponsibility(thisMethod) }
	midiDisconnect { this.subclassResponsibility(thisMethod) }
	// OSC
	setOscCalibration { this.subclassResponsibility(thisMethod) }
	getOscCalibration { this.subclassResponsibility(thisMethod) }
	setOscMapping { this.subclassResponsibility(thisMethod) }
	getOscMapping { this.subclassResponsibility(thisMethod) }
	setOscInputConstraints { this.subclassResponsibility(thisMethod) }
	getOscInputConstraints { this.subclassResponsibility(thisMethod) }
	oscConnect { this.subclassResponsibility(thisMethod) }
	oscDisconnect { this.subclassResponsibility(thisMethod) }

	/*** Initializing models and controllers ***/
	initModels { this.subclassResponsibility(thisMethod) }
	initControllers { this.subclassResponsibility(thisMethod) }
}