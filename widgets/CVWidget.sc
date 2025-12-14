CVWidget {
	classvar <all;
	classvar <>removeResponders = true, <>initMidiOnStartUp = false, <>shortcuts, prefs;
	classvar <>midiMode = 0, <>midiZero = 63, <>midiCtrlButtonGroup = 1;
	classvar <>oscEndless = false, <>oscCalibration = true, <>oscInputRange = #[0.0001, 0.0001];
	classvar <>snapDistance = 0.1, <>resolution = 1, <>inputMapping;
	classvar syncKeysEvent;
	classvar <wmc; // models and controllers tied to the class

	// widget models and controllers
	// defined individually in subclasses
	var <name, <wmc;
	var syncKeysEvent;

	// custom actions
	// to be evaluated on cv.value_ or cv.input_
	var <env; // variables to be used inside actions
	var <widgetActions/*, <userActions*/;

	*initClass {
		var scPrefs = false;

		Class.initClassTree(OSCCommands);
		Class.initClassTree(KeyDownActions);
		// FIXME: CVWidgetShortcuts
		Class.initClassTree(CVWidgetShortcuts);

		this.inputMapping_((mapping: \linlin));

		// all CVWidgets
		#all, wmc = ()!2;
		syncKeysEvent ?? {
			syncKeysEvent = (proto: List[\default], user: List[])
		};

		StartUp.add {
			wmc.midiInitialized = (m: Ref(false));
			wmc.midiSources = (m: Ref(()));
			wmc.isScanningOsc = (m: Ref(false));
			wmc.oscAddrAndCmds = (m: Ref(OSCCommands.ipsAndCmds));
			if (this.initMidiOnStartUp) {
				MIDIClient.init;
				try { MIDIIn.connectAll(false) } { |error|
					error.postln;
					"MIDIIn.connectAll failed. Please establish the necessary connections manually.".warn;
				};
				MIDIClient.externalSources.do { |source|
					if (wmc.midiSources.m.value.includes(source.uid).not) {
						wmc.midiSources.m.value.put("% (%)".format(source.name, source.uid).asSymbol, source.uid)
					};
				};
				wmc.midiInitialized.m.value_(MIDIClient.initialized);
			}
		};

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
			// global
			inputMapping: this.inputMapping,
			snapDistance: this.snapDistance,
			resolution: this.resolution,
			// midi
			midiMode: this.midiMode,
			midiZero: this.midiZero,
			midiCtrlButtonGroup: this.midiCtrlButtonGroup,
			// osc
			oscEndless: this.oscEndless,
			oscCalibration: this.oscCalibration
		)
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
	setMidiZero { this.subclassResponsibility(thisMethod) }
	getMidiZero { this.subclassResponsibility(thisMethod) }
	setMidiSnapDistance { this.subclassResponsibility(thisMethod) }
	getMidiSnapDistance { this.subclassResponsibility(thisMethod) }
	setMidiCtrlButtonGroup { this.subclassResponsibility(thisMethod) }
	getMidiCtrlButtonGroup { this.subclassResponsibility(thisMethod) }
	setMidiResolution { this.subclassResponsibility(thisMethod) }
	getMidiResolution { this.subclassResponsibility(thisMethod) }
	setMidiInputMapping { this.subclassResponsibility(thisMethod) }
	getMidiInputMapping { this.subclassResponsibility(thisMethod) }
	midiConnect { this.subclassResponsibility(thisMethod) }
	midiDisconnect { this.subclassResponsibility(thisMethod) }
	// OSC
	addOscConnector { this.subclassResponsibility(thisMethod) }
	oscDialog { this.subclassResponsibility(thisMethod) }
	removeOscConnector { this.subclassResponsibility(thisMethod) }
	setOscEndless { this.subclassResponsibility(thisMethod) }
	getOscEndless { this.subclassResponsibility(thisMethod) }
	setOscResolution { this.subclassResponsibility(thisMethod) }
	getOscResolution { this.subclassResponsibility(thisMethod) }
	setOscSnapDistance { this.subclassResponsibility(thisMethod) }
	getOscSnapDistance { this.subclassResponsibility(thisMethod) }
	setOscCalibration { this.subclassResponsibility(thisMethod) }
	getOscCalibration { this.subclassResponsibility(thisMethod) }
	setOscInputMapping { this.subclassResponsibility(thisMethod) }
	getOscInputMapping { this.subclassResponsibility(thisMethod) }
	setOscInputConstraints { this.subclassResponsibility(thisMethod) }
	getOscInputConstraints { this.subclassResponsibility(thisMethod) }
	oscConnect { this.subclassResponsibility(thisMethod) }
	oscDisconnect { this.subclassResponsibility(thisMethod) }
	// remove (invalidate) widget
	remove { this.subclassResponsibility(thisMethod) }
	// Initializing models and controllers
	initModels { this.subclassResponsibility(thisMethod) }
	initControllers { this.subclassResponsibility(thisMethod) }

	// global models and controllers
	// private
	*prAddSyncKey { |key, proto|
		var thisKey = key.asSymbol;

		if (proto) {
			syncKeysEvent.proto.add(thisKey)
		} { syncKeysEvent.user.add(thisKey) }
	}

	// private
	*prRemoveSyncKey { |key, proto|
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

	*syncKeys {
		^syncKeysEvent.proto ++ syncKeysEvent.user;
	}

	// instance models and controllers
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

	midiConnectors {
		^wmc.midiConnectors.m.value
	}

	oscConnectors {
		^wmc.oscConnectors.m.value
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
						v.c.put(thisKey, func)
					}
				}
			} {
				thisControllers.do { |c|
					if (wmc[c].notNil and: {
						c != \mapConstrainterHi and: {
							c != \mapConstrainterLo
						}
					}) {
						wmc[c].c.put(thisKey, func)
					}
				}
			}
		};

		this.prAddSyncKey(thisKey, proto);
	}

	// remove controllers that have been added through CVWidget:-extend
	reduce { |key, proto = false|
		var thisKey = key.asSymbol;

		if (key.notNil and: { this.syncKeys.includes(thisKey) }) {
			if ((proto).or(proto.not and: { syncKeysEvent.user.includes(thisKey)})) {
				// recursion.(wmc)
				wmc.pairsDo { |k, v|
					if (k != \mapConstrainterHi and: {
						k != \mapConstrainterLo
					}) {
						v.c !? { v.c.removeAt(thisKey) }
					}
				}
			};
			this.prRemoveSyncKey(thisKey, proto);
		}
	}

}