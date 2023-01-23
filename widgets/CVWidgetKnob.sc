CVWidgetKnob : CVWidget {
	var <cv;
	// ... see if I can move them to CVWidget
	// var <wmc; //widget models and controllers
	var <>numOscConnectors = 0, <>numMidiConnectors = 0;
	var <oscConnectorDialogs, <midiConnectorDialogs;

	*new { |name, cv, setup, action|
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
		#oscConnectorDialogs, midiConnectorDialogs = List[]!2;

		oscConnectors.addDependant({
			// if dropdown menu is present automatically add/remove item to/from items
			oscConnectorDialogs.do { |dialog|
				if (dialog.notNil and:{ dialog.isClosed.not }) {
					dialog.connectionSelect.items_(oscConnectors.collect(_.name));
				}
			}
		});
		midiConnectors.addDependant({
			midiConnectorDialogs.do { |dialog|
				if (dialog.notNil and:{ dialog.isClosed.not }) {
					dialog.connectionSelect.items_(midiConnectors.collect(_.name));
				}
			}
		});

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

	makeMidiConnector { |parent|
		var dialog = MidiConnectorsEditorView(this, parent).front;
		midiConnectorDialogs.add(dialog);
		midiConnectors.changed(\value);
		^dialog;
	}

	makeOscConnector { |parent|
		var dialog = OscConnectorsEditorView(this, parent).front;
		oscConnectorDialogs.add(dialog);
		oscConnectors.changed(\value)
		^dialog;
	}

	// MIDI
	setMidiMode { |mode, connection|
		// connection can be a Symbol or a MidiConnector instance
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			midiConnectors.do(_.setMidiMode(mode))
		} {
			connection.setMidiMode(mode)
		}
	}

	getMidiMode { |connection|
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			^midiConnectors.collect(_.getMidiMode);
		} {
			^connection.getMidiMode;
		}
	}

	setMidiMean { |meanval, connection|
		// connection can be a Symbol or a MidiConnector instance
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			midiConnectors.do(_.setMidiMean(meanval))
		} {
			connection.setMidiMean(meanval)
		}
	}

	getMidiMean { |connection|
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			^midiConnectors.collect(_.getMidiMean)
		} {
			^connection.getMidiMean;
		}
	}

	setSoftWithin { |threshold, connection|
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			midiConnectors.do(_.setSoftWithin(threshold));
		} {
			connection.setSoftWithin(threshold);
		}
	}

	getSoftWithin { |connection|
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			^midiConnectors.collect(_.getSoftWithin);
		} {
			^connection.getSoftWithin;
		}
	}

	setCtrlButtonBank { |numSliders, connection|
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			midiConnectors.do(_.setCtrlButtonBank(numSliders));
		} {
			connection.setCtrlButtonBank(numSliders);
		}
	}

	getCtrlButtonBank {	|connection|
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			^midiConnectors.collect(_.getCtrlButtonBank);
		} {
			^connection.getCtrlButtonBank;
		}
	}

	setMidiResolution { |resolution, connection|
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			midiConnectors.do(_.setMidiResolution(resolution));
		} {
			connection.setMidiResolution(resolution);
		}
	}

	getMidiResolution { |connection|
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		if (connection.isNil) {
			^midiConnectors.collect(_.getMidiResolution);
		} {
			^connection.getMidiResolution;
		}
	}

	midiConnect { |connection, src, chan, num|
		// create new annonymous connection if none is given
		connection ?? {
			connection = MidiConnector(this);
		};
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		// pass execution to connection
		connection.midiConnect(src, chan, num);
	}

	midiDisconnect { |connection|
		connection ?? {
			Error("No connection given. Don't know which connection to disconnect!").throw;
		};
		if (connection.class == Symbol) {
			connection = midiConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};

		// pass execution to connection
		connection.midiDisconnect
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

	removeOscConnector { |connection|
		if (connection.class == Symbol) {
			connection = oscConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnectors[connection]
		};
		// FIXME: should call connection.remove instead?
		connection.oscDisconnect;
		oscConnectors.remove(connection);
	}

	addMidiConnector { |name|
		name !? { name = name.asSymbol };
		^MidiConnector(this, name);
	}

	removeMidiConnector { |connection|
		if (midiConnectors.size > 1) {
			if (connection.class == Symbol) {
				connection = midiConnectors.detect { |c| c.name == connection }
			};
			if (connection.isInteger) {
				connection = midiConnectors[connection]
			};
			connection.remove;
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

}