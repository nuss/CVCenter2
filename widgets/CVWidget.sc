CVWidget {
	classvar <all;
	classvar <>removeResponders = true, <>midiSources, <>shortcuts, prefs;
	classvar <>midiMode = 0, <>midiResolution = 1, <>midiMean = 64, <>ctrlButtonBank, <>softWithin = 0.1;
	classvar <>oscCalibration = true;

	// widget models and controllers
	// defined individually in subclasses
	var <name, <wmc;
	var syncKeysEvent;

	// custom actions
	// to be evaluated on cv.value_ or cv.input_
	var <env; // variables to be used inside actions
	var <widgetActions/*, <userActions*/;
	var <oscConnectors, <midiConnectors;

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

		if (scPrefs.not, {
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

	/*** common interface ***/

	// the CV's ControlSpec
	setSpec { this.subclassResponsibility(thisMethod) }
	getSpec { this.subclassResponsibility(thisMethod) }
	// CV actions
	addAction { this.subclassResponsibility(thisMethod) }
	removeAction { this.subclassResponsibility(thisMethod) }
	activateAction { this.subclassResponsibility(thisMethod) }
	updateAction { this.subclassResponsibility(thisMethod) }
	// MIDI
	addMidiConnector { this.subclassResponsibility(thisMethod) }
	midiDialog { this.subclassResponsibility(thisMethod) }
	removeMidiConnector { this.subclassResponsibility(thisMethod) }
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
	addOscConnector { this.subclassResponsibility(thisMethod) }
	oscDialog { this.subclassResponsibility(thisMethod) }
	removeOscConnector { this.subclassResponsibility(thisMethod) }
	setOscCalibration { this.subclassResponsibility(thisMethod) }
	getOscCalibration { this.subclassResponsibility(thisMethod) }
	setOscMapping { this.subclassResponsibility(thisMethod) }
	getOscMapping { this.subclassResponsibility(thisMethod) }
	setOscInputConstraints { this.subclassResponsibility(thisMethod) }
	getOscInputConstraints { this.subclassResponsibility(thisMethod) }
	oscConnect { this.subclassResponsibility(thisMethod) }
	oscDisconnect { this.subclassResponsibility(thisMethod) }
	remove { this.subclassResponsibility(thisMethod) }
	// Initializing models and controllers
	initModels { this.subclassResponsibility(thisMethod) }
	initControllers { this.subclassResponsibility(thisMethod) }


	// private
	prAddSyncKey { |key, proto|
		var thisKey = key.asSymbol;

		if (proto) {
			syncKeysEvent.proto.add(thisKey)
		} { syncKeysEvent.user.add(thisKey) }
	}

	// private
	prRemoveSyncKey { |key, proto|
		var thisKey = key.asSymbol;

		if (proto) {
			if (syncKeysEvent.proto.includes(thisKey)) {
				syncKeysEvent.proto.remove(thisKey)
			}
		} {
			if (syncKeysEvent.user.includes(thisKey)) {
				syncKeysEvent.user.remove(thisKey)
			}
		}
	}

	syncKeys {
		^syncKeysEvent.proto ++ syncKeysEvent.user;
	}

	// extend the API with custom controllers
	extend { |key, func, controllers, proto=false|
		var thisKey, thisControllers;
		thisKey = key.asSymbol;
		thisControllers = controllers.collect({ |c| c.asSymbol });
		if (this.syncKeys.includes(thisKey)) {
			Error("Sync key '%' is already in use!".format(thisKey)).throw
		} {
			// controllers -> must be a list of existing controllers
			if (controllers.size == 0) {
				wmc.pairsDo { |k, v|
					if (k != \mapConstrainterHi and:{
						k != \mapConstrainterLo
					}) {
						v.controller.put(thisKey, func)
					}
				}
			} {
				thisControllers.do { |c|
					if (wmc[c].notNil and: {
						c != \mapConstrainterHi and: {
							c != \mapConstrainterLo
						}
					}) {
						wmc[c].controller.put(thisKey, func)
					}
				}
			}
		};

		this.prAddSyncKey(thisKey, proto);
	}

	// remove controllers that have been added through CVWidget:-extend
	reduce { |key, proto=false|
		var thisKey = key.asSymbol;

		if (key.notNil and: { this.syncKeys.includes(thisKey) }) {
			if ((proto).or(proto.not and: { syncKeysEvent.user.includes(thisKey)})) {
				// recursion.(wmc)
				wmc.pairsDo { |k, v|
					if (k != \mapConstrainterHi and: {
						k != \mapConstrainterLo
					}) {
						v.controller.removeAt(thisKey)
					}
				}
			};
			this.prRemoveSyncKey(thisKey, proto);
		}
	}

}