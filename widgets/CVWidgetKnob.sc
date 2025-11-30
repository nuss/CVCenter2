CVWidgetKnob : CVWidget {
	var <cv;
	// only needed for naming a connector
	var <>numOscConnectors = 0, <>numMidiConnectors = 0;

	*new { |name, cv, setup, action, modelsAndControllers|
		^super.new.init(name, cv, setup, action);
	}

	init { |wdgtName, wdgtCV, setupArgs, action, modelsControllers|
		wdgtName ?? {
			Error("No name provided for new CVWidgetKnob").throw;
		};

		name = wdgtName.asSymbol;

		if (wdgtCV.isNil) { cv = CV.new } { cv = wdgtCV };

		syncKeysEvent ?? {
			syncKeysEvent = (proto: List[\default], user: List[])
		};

		all[name] ?? { all.put(name, this) };
		// #oscConnectors, midiConnectors = List[]!2;

		// oscConnectors.addDependant({
		// 	OscConnectorSelect.all[this].do { |select|
		// 		select.view.items_(oscConnectors.collect(_.name) ++ ['addOscConnector...'])
		// 	}
		// });
		// midiConnectors.addDependant({
		// 	MidiConnectorSelect.all[this].do { |select|
		// 		select.view.items_(midiConnectors.collect(_.name) ++ ['add MidiConnector...']);
		// 	}
		// });

		setupArgs !? {
			setupArgs.isKindOf(Dictionary).not.if {
				Error("a setup has to be provided as a Dictionary or an Event").throw
			};
			this.setMidiMode(setupArgs[\midiMode] ? this.class.midiMode);
			this.setMidiResolution(setupArgs[\midiResolution] ? this.class.resolution);
			this.setMidiZero(setupArgs[\midiMean] ? this.class.midiMean);
			this.setMidiCtrlButtonGroup(setupArgs[\midiCtrlButtonBank]);
			this.setMidiSnapDistance(setupArgs[\snapDistance] ? this.class.snapDistance);
			this.setOscCalibration(setupArgs[\oscCalibration] ? this.class.oscCalibration);
			this.setOscEndless(setupArgs[\oscEndless] ? this.class.oscEndless);
			this.setOscResolution(setupArgs[\oscResolution] ? this.class.resolution);
		};

		// an Event to be used for variables defined outside actions
		env = ();
		// the functions that will be evaluated by a SimpleController that's added by calling addAction
		widgetActions = ();
		// the user-supplied actions, added as argument to addAction
		// the actions are evaluated within the outer widgetAction
		// userActions = ();
		// add a 'default' action, if given
		action !? { this.addAction(\default, action) };

		if (modelsControllers.notNil) {
			wmc = modelsControllers
		} {
			wmc ?? { wmc = () }
		};
		this.initConnectors(modelsControllers);
		this.initModels(modelsControllers);
	}

	initConnectors { |modelsControllers|
		wmc.midiConnectors ?? { wmc.midiConnectors = () };
		wmc.midiConnectors.m ?? {
			wmc.midiConnectors.m = Ref(List[])
		};
		wmc.oscConnectors ?? { wmc.oscConnectors = () };
		wmc.oscConnectors.m ?? {
			wmc.oscConnectors.m = Ref(List[])
		}
	}

	initModels { |modelsControllers|
		// models, not tied to connectors, global to all
		// MIDI and OSC connections
		wmc.cvSpec ?? { wmc.cvSpec = () };
		wmc.cvSpec.m ?? {
			wmc.cvSpec.m = Ref(this.getSpec);
		};

		wmc.actions ?? { wmc.actions = () };
		wmc.actions.m ?? {
			wmc.actions.m = Ref((numActions: 0, activeActions: 0))
		};

		this.initControllers(wmc);

		// every new CVWidget should
		// immediately be amended by
		// an empty OscConnector
		// resp. an empty MidiConnector
		// controllers for connectors
		// are added within these classes
		OscConnector(this);
		MidiConnector(this);
	}

	initControllers { |wmc|
		#[
			prInitSpecControl,
			prInitActionsControl
		].do { |method|
			this.perform(method, wmc, cv)
		}
	}

	// the CV's ControlSpec
	setSpec { |spec|
		if ((spec = spec.asSpec).isKindOf(ControlSpec).not) {
			Error("No valid ControlSpec given for setSpec.").throw;
		};
		wmc.cvSpec.m.value_(spec).changedPerformKeys(this.syncKeys);
	}

	getSpec {
		^this.cv.spec;
	}

	// CV actions
	addAction { |name, action, active=true|
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
			"The function you have provided contains variables that have been defined outside the function (\"open Function\"). As such it cannot be stored with a setup!".warn;
		};

		widgetActions.put(name, nil -> nil);
		if (action.class == String) {
			widgetActions[name].value = [action, active];
		} {
			widgetActions[name].value = [action.asCompileString, active];
		};

		if (active) {
			widgetActions[name].key = cv.addController({ |cv|
				widgetActions[name].value[0].interpret.value(cv, this)
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
						widgetActions[name].value[0].interpret.value(cv, this)
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

	midiDialog { |connector=0, parent|
		^MidiConnectorsEditorView(this, connector, parent).front;
	}

	oscDialog { |connector=0, parent|
		^OscConnectorsEditorView(this, connector, parent).front;
	}

	// MIDI
	setMidiMode { |mode, connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			wmc.midiConnectors.m.value.do(_.setMidiMode(mode))
		} {
			connector.setMidiMode(mode)
		}
	}

	getMidiMode { |connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			^wmc.midiConnectors.m.value.collect(_.getMidiMode);
		} {
			^connector.getMidiMode;
		}
	}

	setMidiZero { |zeroval, connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			wmc.midiConnectors.m.value.do(_.setMidiZero(zeroval))
		} {
			connector.setMidiZero(zeroval)
		}
	}

	getMidiZero { |connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			^wmc.midiConnectors.m.value.collect(_.getMidiZero)
		} {
			^connector.getMidiZero;
		}
	}

	setMidiSnapDistance { |snapDistance, connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			wmc.midiConnectors.m.value.do(_.setMidiSnapDistance(snapDistance));
		} {
			connector.setMidiSnapDistance(snapDistance);
		}
	}

	getMidiSnapDistance { |connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			^wmc.midiConnectors.m.value.collect(_.getMidiSnapDistance);
		} {
			^connector.getMidiSnapDistance;
		}
	}

	setMidiCtrlButtonGroup { |numButtons, connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			wmc.midiConnectors.m.value.do(_.setMidiCtrlButtonGroup(numButtons));
		} {
			connector.setMidiCtrlButtonGroup(numButtons);
		}
	}

	getMidiCtrlButtonGroup { |connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			^wmc.midiConnectors.m.value.collect(_.getMidiCtrlButtonGroup);
		} {
			^connector.getMidiCtrlButtonGroup;
		}
	}

	setMidiResolution { |resolution, connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			wmc.midiConnectors.m.value.do(_.setMidiResolution(resolution));
		} {
			connector.setMidiResolution(resolution);
		}
	}

	getMidiResolution { |connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			^wmc.midiConnectors.m.value.collect(_.getMidiResolution)
		} {
			^connector.getMidiResolution
		}
	}

	setMidiInputMapping { |mapping, curve = 0, env(Env([0, 1], [1])), connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			wmc.midiConnectors.m.value.do(_.setMidiInputMapping(mapping, curve, env))
		} {
			connector.setMidiInputMapping(mapping, curve, env)
		}
	}

	getMidiInputMapping { |connector|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		if (connector.isNil) {
			^wmc.midiConnectors.m.value.collect(_.getMidiInputMapping)
		} {
			^connector.getMidiInputMapping
		}
	}

	midiConnect { |connector, src, chan, num|
		// create new annonymous connector if none is given
		connector ?? {
			if (wmc.midiConnectors.m.value.size == 1 and: {
				wmc.midiConnections.m.value[0].isNil
			}) {
				connector = wmc.midiConnectors.m.value[0]
			} {
				connector = MidiConnector(this)
			}
		};
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		// pass execution to connector
		connector.midiConnect(src, chan, num);
	}

	midiDisconnect { |connector|
		connector ?? {
			Error("No connector given. Don't know which connector to disconnect!").throw;
		};
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};

		// pass execution to connector
		connector.midiDisconnect
	}

	// OSC
	setOscCalibration {}
	getOscCalibration {}
	setOscMapping {}
	getOscMapping {}
	setOscInputConstraints {}
	getOscInputConstraints {}
	oscConnect {}
	oscDisconnect {}

	// connections handling
	addOscConnector { |name|
		name !? { name = name.asSymbol };
		^OscConnector(this, name);
	}

	removeOscConnector { |connector, forceAll = false|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};
		connector.remove(forceAll);
	}

	addMidiConnector { |name|
		name !? { name = name.asSymbol };
		^MidiConnector(this, name);
	}

	removeMidiConnector { |connector, forceAll = false|
		if (connector.isInteger) {
			connector = wmc.midiConnectors.m.value[connector]
		};
		connector.remove(forceAll);
	}

	remove {
		this.midiConnectors.reverse.do(_.remove(true));
		this.oscConnectors.reverse.do(_.remove(true));
		// SimpleControllers should be removed explicitely
		this.widgetActions.do { |asoc|
			asoc.key.remove;
		};
		// remove the widget's controllers from Object.dependantsDictionary
		this.wmc.do { |val| val.c.remove };
		all.removeAt(name);
	}

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

	storeOn { |stream|
		stream << this.class.name << "(" <<* [
			this.name.cs,
			this.cv
		] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}

}