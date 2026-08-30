CVWidget {
	classvar <all;
	classvar <>removeResponders = true, <>initMidiOnStartUp = false, <>shortcuts, prefs;
	classvar <>midiMode = 0, <>midiZero = 64, <>midiCtrlButtonGroup = 1;
	classvar <>oscEndless = false, <>oscCalibration = true, <>oscInputRange = #[0.0001, 0.0001], <>oscMatching = false;
	classvar <>snapDistance = 0, <>resolution = 1, <>inputMapping;
	classvar syncKeysEvent;
	classvar <wmc; // models and controllers tied to the class

	// widget models and controllers
	// defined individually in subclasses
	// one CV for each CVWidegt - CVWidgetKnob or CVWidgetMS
	var <name, <cv, <wmc;
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
		// TODO: all should be separated into 'proto' -> widgets that shouldn't be removed, 'and 'user' -> widgets created by users
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
			oscCalibration: this.oscCalibration,
			oscMatching: this.oscMatching
		)
	}

	/*** common interface ***/

	// the CV's ControlSpec
	setSpec { this.subclassResponsibility(thisMethod) }
	// getSpec { this.subclassResponsibility(thisMethod) }
	// // CV actions
	// addAction { this.subclassResponsibility(thisMethod) }
	// removeAction { this.subclassResponsibility(thisMethod) }
	// activateAction { this.subclassResponsibility(thisMethod) }
	// updateAction { this.subclassResponsibility(thisMethod) }
	// MIDI
	// midiConnectors { this.subclassResponsibility(thisMethod) }
	addMidiConnector { this.subclassResponsibility(thisMethod) }
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
	setMidiTemplate { this.subclassResponsibility(thisMethod) }
	getMidiTemplate { this.subclassResponsibility(thisMethod) }
	setMidiDispatcher { this.subclassResponibility(thisMethod) }
	getMidiDispatcher { this.subclassResponsibility(thisMethod) }
	setMIDIFuncEnabled { this.subclassResponsibility(thisMethod) }
	getMIDIFuncEnabled { this.subclassResponsibility(thisMethod) }
	midiConnect { this.subclassResponsibility(thisMethod) }
	midiDisconnect { this.subclassResponsibility(thisMethod) }

	// OSC
	// oscConnectors { this.subclassResponsibility(thisMethod) }
	addOscConnector { this.subclassResponsibility(thisMethod) }
	removeOscConnector { this.subclassResponsibility(thisMethod) }
	setOscEndless { this.subclassResponsibility(thisMethod) }
	getOscEndless { this.subclassResponsibility(thisMethod) }
	setOscResolution { this.subclassResponsibility(thisMethod) }
	getOscResolution { this.subclassResponsibility(thisMethod) }
	setOscSnapDistance { this.subclassResponsibility(thisMethod) }
	getOscSnapDistance { this.subclassResponsibility(thisMethod) }
	setOscCalibration { this.subclassResponsibility(thisMethod) }
	getOscCalibration { this.subclassResponsibility(thisMethod) }
	resetOscCalibration { this.subclassResponsibility(thisMethod) }
	setOscInputMapping { this.subclassResponsibility(thisMethod) }
	getOscInputMapping { this.subclassResponsibility(thisMethod) }
	setOscInputConstraints { this.subclassResponsibility(thisMethod) }
	getOscInputConstraints { this.subclassResponsibility(thisMethod) }
	setOscCmdName { this.subclassResponsibility(thisMethod) }
	getOscCmdName { this.subclassResponsibility(thisMethod) }
	setOscMsgIndex { this.subclassResponsibility(thisMethod) }
	getOscMsgIndex { this.subclassResponsibility(thisMethod) }
	setOscMatching { this.subclassResponsibility(thisMethod) }
	getOscMatching { this.subclassResponsibility(thisMethod) }
	setOscTemplate { this.subclassResponsibility(thisMethod) }
	getOscTemplate { this.subclassResponsibility(thisMethod) }
	setOscDispatcher { this.subclassResponibility(thisMethod) }
	getOscDispatcher { this.subclassResponsibility(thisMethod) }
	setOSCFuncEnabled { this.subclassResponsibility(thisMethod) }
	getOSCFuncEnabled { this.subclassResponsibility(thisMethod) }
	oscConnect { this.subclassResponsibility(thisMethod) }
	oscDisconnect { this.subclassResponsibility(thisMethod) }
	// remove (invalidate) widget
	remove { this.subclassResponsibility(thisMethod) }
	// Initializing models and controllers
	initModels { this.subclassResponsibility(thisMethod) }
	// initControllers { this.subclassResponsibility(thisMethod) }

	initControllers { |wmc|
		#[
			prInitSpecControl,
			prInitActionsControl
		].do { |method|
			this.perform(method, wmc, this.cv)
		}
	}


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

	syncKeys {
		^syncKeysEvent.proto ++ syncKeysEvent.user;
	}

	// extend the API with custom controllers
	extend { |key, func, wmcKeys, proto=false|
		key ?? { Error("%: No argument 'key' not given.".format(thisMethod)).throw };
		func ?? { Error("%: No argument 'func' not given.".format(thisMethod)).throw };
		key = key.asSymbol;
		wmcKeys = wmcKeys.collect(_.asSymbol);
		if (this.syncKeys.includes(key)) {
			Error("Sync key '%' is already in use!".format(key)).throw
		} {
			// controllers -> must be a list of existing controllers
			if (wmcKeys.size == 0) {
				wmc.pairsDo { |k, v|
					if (k !== \oscInputConstrainters) {
						v.c !? { v.c.put(key, func) }
					}
				}
			} {
				wmcKeys.do { |c|
					wmc[c] !? { wmc[c].c.put(key, func) }
				}
			}
		};
		this.prAddSyncKey(key, proto);
	}

	// remove controllers that have been added through CVWidget:-extend
	reduce { |key, proto = false|
		key ?? { Error("%: No argument 'key' not given.".format(thisMethod)).throw };
		key = key.asSymbol;
		if (key.notNil and: { this.syncKeys.includes(key) }) {
			if ((proto).or(proto.not and: { syncKeysEvent.user.includes(key)})) {
				// recursion.(wmc)
				wmc.pairsDo { |k, v|
					if (k !== \oscInputConstrainters) {
						v.c !? { v.c.removeAt(key) }
					}
				}
			};
			this.prRemoveSyncKey(key, proto);
		}
	}

	// same logic within CVWidgetKnob and CVWidgetMS
	getSpec {
		^this.wmc.cvSpec.m.value
		// ^this.cv.spec;
	}

	// CV actions
	addAction { |name, action, active(true)|
		var testAction;
		name ?? { Error("Please provide a name under which the action will be added to the widget").throw };
		name = name.asSymbol;
		widgetActions[name] !? {
			Error("An action under the given name '%' already exists. Please choose a different name".format(name)).throw;
		};
		action ?? { Error("Please provide an action!").throw };
		if (action.isFunction.not and:{
			action.class !== FunctionList and:{
				action.interpret.isFunction.not
			}
		}, {
			Error("'action' must be a Function/FunctionList or a string that compiles to a Function or a FunctionList").throw;
		});
		if (action.class == String) { testAction = action.interpret } { testAction = action };
		if (testAction.isClosed.not) {
			"The function you have provided contains variables that have been declared outside the function (\"open Function\"). As such it cannot be stored with a setup!".warn;
		};

		widgetActions.put(name, nil -> nil);
		if (action.class == String) {
			widgetActions[name].value = [action, active];
		} {
			widgetActions[name].value = [action.asCompileString, active];
		};

		if (active) {
			widgetActions[name].key = cv.addController({ |cv|
				widgetActions[name].value[0].interpret.value(this)
			})
		};

		wmc.actions.m.value_((
			numActions: widgetActions.size,
			activeActions: widgetActions.select { |asoc| asoc.value[1] == true }.size
		)).changedPerformKeys(this.syncKeys);

		// TODO: Take care of editor views
	}

	removeAction { |name|
		name ?? {
			"Please provide a name of an existing action!".error;
			^nil
		};
		name = name.asSymbol;
		widgetActions[name] !? {
			if (widgetActions[name].key.class == SimpleController) {
				widgetActions[name].key.remove
			};
			widgetActions.removeAt(name);
			wmc.actions.m.value_((
				numActions: widgetActions.size,
				activeActions: widgetActions.select { |asoc| asoc.value[1] == true }.size
			)).changedPerformKeys(this.syncKeys);
		};

		// TODO: Take care of editor views
	}

	activateAction { |name, activate=true|
		var action, containerFunc, controller;
		name ?? {
			"Please provide the action's name!".error;
			^nil
		};
		name = name.asSymbol;
		widgetActions[name] !? {
			action = widgetActions[name].value[0];
			if (activate) {
				// avoid memory leak, only create new SimpleController if key is nil!
				widgetActions[name].key ?? {
					widgetActions[name].key = cv.addController({ |cv|
						widgetActions[name].value[0].interpret.value(this)
					})
				}
			} {
				if (widgetActions[name].key.class == SimpleController) {
					widgetActions[name].key.remove;
					widgetActions[name].key = nil;
				}
			};
			widgetActions[name].value[1] = activate;
			wmc.actions.m.value_((
				numActions: widgetActions.size,
				activeActions: widgetActions.select { |asoc| asoc.value[1] == true }.size
			)).changedPerformKeys(this.syncKeys);
		};

		// TODO: Take care of editor views
	}

	updateAction { |name, action|
		var testAction;
		if (name.isNil or: { widgetActions[name].isNil }) {
			Error("Please provide a name of an already existing action!").throw
		};
		name = name.asSymbol;
		if (action.class == String) { testAction = action.interpret } { testAction = action };
		if (testAction.isClosed.not) {
			"The function you have provided contains variables that have been defined outside the function (\"open Function\"). As such it cannot be stored with a setup!".warn;
		};
		widgetActions[name] !? {
			if (action.class == String) {
				widgetActions[name].value[0] = action
			} {
				widgetActions[name].value[0] = action.asCompileString
			}
		};
		wmc.actions.m.value_((
			numActions: widgetActions.size,
			activeActions: widgetActions.select { |asoc| asoc.value[1] == true }.size
		)).changedPerformKeys(this.syncKeys);

		// TODO: Take care of editor views
	}

	// PRIVAT
	// init controllers (private)
	prInitSpecControl { |wmc, cv|
		wmc.cvSpec.c ?? {
			wmc.cvSpec.c = SimpleController(wmc.cvSpec.m);
		};
		wmc.cvSpec.c.put(\default, { |changer, what, moreArgs|
			this.cv.spec_(changer.value);
		})
	}

	prInitActionsControl { |wmc, cv|
		wmc.actions.c ?? {
			wmc.actions.c = SimpleController(wmc.actions.m);
		};
		wmc.actions.c.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}
}