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
		#oscConnectors, midiConnectors = List[]!2;

		oscConnectors.addDependant({
			// if dropdown menu is present automatically add/remove item to/from items
			OscConnectorSelect.all[this].do { |select|
				select.view.items_(["Select connector..."] ++ oscConnectors.collect(_.name))
			}
		});
		midiConnectors.addDependant({
			MidiConnectorSelect.all[this].do { |select|
				select.view.items_(midiConnectors.collect(_.name) ++ ["add MidiConnector..."]);
			}
		});

		setupArgs !? {
			setupArgs.isKindOf(Dictionary).not.if {
				Error("a setup has to be provided as a Dictionary or an Event").throw
			};
			this.setMidiMode(setupArgs[\midiMode] ? this.class.midiMode);
			this.setMidiResolution(setupArgs[\midiResolution] ? this.class.midiResolution);
			this.setMidiZero(setupArgs[\midiMean] ? this.class.midiMean);
			this.setCtrlButtonGroup(setupArgs[\ctrlButtonBank]);
			this.setSnapDistance(setupArgs[\softWithin] ? this.class.softWithin);
			this.setOscCalibration(setupArgs[\oscCalibration] ? this.class.oscCalibration);
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

		this.initModels(modelsControllers);
	}

	initModels { |modelsControllers|
		if (modelsControllers.notNil) {
			wmc = modelsControllers
		} {
			wmc ?? { wmc = () }
		};

		// TODO: should be separate in CVWidget class because CVWidget2D needs two...
		wmc.cvSpec ?? { wmc.cvSpec = () };
		wmc.cvSpec.model ?? {
			wmc.cvSpec.model = Ref(this.getSpec);
		};

		wmc.actions ?? { wmc.actions = () };
		wmc.actions.model ?? {
			wmc.actions.model = Ref((numActions: 0, activeActions: 0))
		};


		// should probly go to an appropriate place in the widget's view
		/*wmc.cvGuiConnections ?? { wmc.cvGuiConnections = () };
		wmc.cvGuiConnections.model ?? {
		wmc.cvGuiConnections.model = Ref([true, true])
		};*/

		this.initControllers(wmc);
		// every new CVWidget should
		// immediately be amended by
		// an empty OscConnector
		// resp. an empty MidiConnector
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
		wmc.cvSpec.model.value_(spec).changedKeys(this.syncKeys);
	}

	getSpec {
		^cv.spec;
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

		wmc.actions.model.value_((
			numActions: widgetActions.size,
			activeActions: widgetActions.select { |asoc| asoc.value[1] == true }.size
		)).changedKeys(this.syncKeys);

		// TODO: Take care of editor views
	}

	removeAction { |name|
		name ?? { Error("Please provide the action's name!").throw };
		name = name.asSymbol;
		widgetActions[name] !? {
			if (widgetActions[name].key.class == SimpleController) {
				widgetActions[name].key.remove
			};
			widgetActions.removeAt(name);
			wmc.actions.model.value_((
				numActions: widgetActions.size,
				activeActions: widgetActions.select { |asoc| asoc.value[1] == true }.size
			)).changedKeys(this.syncKeys);
		};

		// TODO: Take care of editor views
	}

	activateAction { |name, activate=true|
		var action, containerFunc, controller;
		name ?? { Error("Please provide the action's name!").throw };
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
			wmc.actions.model.value_((
				numActions: widgetActions.size,
				activeActions: widgetActions.select { |asoc| asoc.value[1] == true }.size
			)).changedKeys(this.syncKeys);
		};

		// TODO: Take care of editor views
	}

	updateAction { |name, action|
		var testAction;
		name ?? { Error("Please provide the action's name!").throw };
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
		wmc.actions.model.value_((
			numActions: widgetActions.size,
			activeActions: widgetActions.select { |asoc| asoc.value[1] == true }.size
		)).changedKeys(this.syncKeys);

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
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			midiConnectors.do(_.setMidiMode(mode))
		} {
			connector.setMidiMode(mode)
		}
	}

	getMidiMode { |connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			^midiConnectors.collect(_.getMidiMode);
		} {
			^connector.getMidiMode;
		}
	}

	setMidiZero { |zeroval, connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			midiConnectors.do(_.setMidiZero(zeroval))
		} {
			connector.setMidiZero(zeroval)
		}
	}

	getMidiZero { |connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			^midiConnectors.collect(_.getMidiZero)
		} {
			^connector.getMidiZero;
		}
	}

	setSnapDistance { |snapDistance, connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			midiConnectors.do(_.setSnapDistance(snapDistance));
		} {
			connector.setSnapDistance(snapDistance);
		}
	}

	getSnapDistance { |connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			^midiConnectors.collect(_.getSnapDistance);
		} {
			^connector.getSnapDistance;
		}
	}

	setCtrlButtonGroup { |numButtons, connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			midiConnectors.do(_.setCtrlButtonGroup(numButtons));
		} {
			connector.setCtrlButtonGroup(numButtons);
		}
	}

	getCtrlButtonGroup { |connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			^midiConnectors.collect(_.getCtrlButtonGroup);
		} {
			^connector.getCtrlButtonGroup;
		}
	}

	setMidiResolution { |resolution, connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			midiConnectors.do(_.setMidiResolution(resolution));
		} {
			connector.setMidiResolution(resolution);
		}
	}

	getMidiResolution { |connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		if (connector.isNil) {
			^midiConnectors.collect(_.getMidiResolution);
		} {
			^connector.getMidiResolution;
		}
	}

	midiConnect { |connector, src, chan, num|
		// create new annonymous connector if none is given
		connector ?? {
			connector = MidiConnector(this);
		};
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};

		// pass execution to connector
		connector.midiConnect(src, chan, num);
	}

	midiDisconnect { |connector|
		connector ?? {
			Error("No connector given. Don't know which connector to disconnect!").throw;
		};
		if (connector.isInteger) {
			connector = midiConnectors[connector]
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

	removeOscConnector { |connector|
		if (connector.isInteger) {
			connector = midiConnectors[connector]
		};
		// FIXME: should call connector.remove instead?
		connector.oscDisconnect;
		oscConnectors.remove(connector);
	}

	addMidiConnector { |name|
		name !? { name = name.asSymbol };
		^MidiConnector(this, name);
	}

	removeMidiConnector { |connector|
		if (midiConnectors.size > 1) {
			if (connector.isInteger) {
				connector = midiConnectors[connector]
			};
			connector.remove;
		}
	}

	remove {
		this.midiConnectors.do(_.remove);
		this.oscConnectors.do(_.remove);
		// SimpleControllers should be removed explicitely
		this.widgetActions.do { |asoc|
			asoc.key.remove;
		};
		all.removeAt(name);
	}

	// init controllers (private)
	prInitSpecControl { |wmc, cv|
		wmc.cvSpec.controller ?? {
			wmc.cvSpec.controller = SimpleController(wmc.cvSpec.model);
		};
		wmc.cvSpec.controller.put(\default, { |changer, what, moreArgs|
			cv.spec_(changer.value);
		})
	}

	prInitActionsControl { |wmc, cv|
		wmc.actions.controller ?? {
			wmc.actions.controller = SimpleController(wmc.actions.model);
		};
		wmc.actions.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<< [
			this.name,
			this.cv,
			(
				midiMode: this.getMidiMode,
				midiResolution: this.getMidiResolution,
				midiZero: this.getMidiZero,
				ctrlButtonGroup: this.getCtrlButtonGroup,
				snapDistance: this.getSnapDistance,
				oscCalibration: this.getOscCalibration
			),
			{},
			this.wmc
		] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}

}