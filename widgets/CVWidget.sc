CVWidget {
	classvar <all;
	classvar <>removeResponders = true, <>midiSources, <>shortcuts, prefs;
	classvar <>midiMode = 0, <>midiResolution = 1, <>midiMean = 0.1, <>ctrlButtonBank, <>softWithin = 0.1;
	classvar <>oscCalibration = true;

	// widget models and controllers
	// defined individually in subclasses
	var <wmc;
	var syncKeysEvent;

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
		};
		if (syncKeysEvent.user.includes(thisKey)) {
			syncKeysEvent.user.remove(thisKey)
		}
	}

	syncKeys {
		^syncKeysEvent.proto ++ syncKeysEvent.user;
	}

	// extend the API with custom controllers
	extend { |key, func, controllers, proto=false|
		var thisKey, thisControllers;
		var recursion = { |col, ctrl|
			case
			{ col.class == Event } {
				col.pairsDo { |k, v|
					if (v.class == List) { recursion.(v, ctrl) };
					if (v.class == Event) {
						if (v.controller.isNil) {
							recursion.(v, ctrl)
						} {
							if (k != \mapConstrainterHi and: {
								k != \mapConstrainterLo
							}) {
								if (ctrl.isNil or: { k === ctrl }) {
									v.controller.put(thisKey, func)
								}
							}
						}
					}
				}
			}
			{ col.class == List } {
				col.do { |it|
					if ((it.class == Event).or(it.class == List)) {
						recursion.(it, ctrl)
					}
				}
			}
		};

		thisKey = key.asSymbol;
		thisControllers = controllers.collect({ |c| c.asSymbol });
		if (this.syncKeys.includes(thisKey)) {
			Error("Sync key '%' is already in use!".format(thisKey)).throw
		} {
			// controllers -> must be a list of existing controllers
			if (controllers.size == 0) {
				// models and controllers are not a simple list of model/controller
				// pairs - it will contain sub-Events for each Midi-/OscConnection
				recursion.(wmc)
			} {
				thisControllers.do { |c| recursion.(wmc, c) }
			}
		};

		this.prAddSyncKey(thisKey, proto);
	}

	// remove controllers that have been added through CVWidget:-extend
	reduce { |key, proto=false|
		var thisKey = key.asSymbol;
		var recursion = { |col|
			case
			{ col.class == Event } {
				col.pairsDo { |k, v|
					if (v.class == List) { recursion.(v) };
					if (v.class == Event) {
						if (v.controller.isNil) {
							recursion.(v)
						} {
							if (k != \mapConstrainterHi and: {
								k != \mapConstrainterLo
							}) {
								v.controller.removeAt(thisKey)
							}
						}
					}
				}
			}
			{ col.class == List } {
				col.do { |it|
					if ((it.class == Event).or(it.class == List)) {
						recursion.(it)
					}
				}
			}
		};

		if (key.notNil and: { this.syncKeys.includes(thisKey) }) {
			if ((proto).or(proto.not and: { syncKeysEvent.user.includes(thisKey)})) {
				recursion.(wmc)
			};
			this.prRemoveSyncKey(thisKey, proto);
		}
	}

}