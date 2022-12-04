CVWidgetKnob : CVWidget {
	var <name, <cv;
	// ... see if I can move them to CVWidget
	// var <wmc; //widget models and controllers
	var <>numOscConnections = 0, <>numMidiConnections = 0;
	var oscConnectionsDialog, midiConnectionsDialog;

	var <oscConnections, <midiConnections;

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
		#oscConnections, midiConnections = List[]!2;
		oscConnections.addDependant({
			// if dropdown menu is present automatically add/remove item to/from items
			if (oscConnectionsDialog.notNil and:{ oscConnectionsDialog.isClosed.not }) {
				oscConnectionsDialog.conSelect.items_(oscConnections.collect(_.name));
			};
		});
		midiConnections.addDependant({
			if (midiConnectionsDialog.notNil and:{ midiConnectionsDialog.isClosed.not }) {
				midiConnectionsDialog.conSelect.items_(midiConnections.collect(_.name));
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
		// an empty OscConnection
		// resp. an empty MidiConnection
		OscConnection(this);
		MidiConnection(this);
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

	// MIDI
	setMidiMode { |mode, connection|
		// connection can be a Symbol or a MidiConnection instance
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			midiConnections.do(_.setMidiMode(mode))
		} {
			connection.setMidiMode(mode)
		}
	}

	getMidiMode { |connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			^midiConnections.collect(_.getMidiMode);
		} {
			^connection.getMidiMode;
		}
	}

	setMidiMean { |meanval, connection|
		// connection can be a Symbol or a MidiConnection instance
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			midiConnections.do(_.setMidiMean(meanval))
		} {
			connection.setMidiMean(meanval)
		}
	}

	getMidiMean { |connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			^midiConnections.collect(_.getMidiMean)
		} {
			^connection.getMidiMean;
		}
	}

	setSoftWithin { |threshold, connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			midiConnections.do(_.setSoftWithin(threshold));
		} {
			connection.setSoftWithin(threshold);
		}
	}

	getSoftWithin { |connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			^midiConnections.collect(_.getSoftWithin);
		} {
			^connection.getSoftWithin;
		}
	}

	setCtrlButtonBank { |numSliders, connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			midiConnections.do(_.setCtrlButtonBank(numSliders));
		} {
			connection.setCtrlButtonBank(numSliders);
		}
	}

	getCtrlButtonBank {	|connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			^midiConnections.collect(_.getCtrlButtonBank);
		} {
			^connection.getCtrlButtonBank;
		}
	}

	setMidiResolution { |resolution, connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			midiConnections.do(_.setMidiResolution(resolution));
		} {
			connection.setMidiResolution(resolution);
		}
	}

	getMidiResolution { |connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			^midiConnections.collect(_.getMidiResolution);
		} {
			^connection.getMidiResolution;
		}
	}

	midiConnect { |connection, src, chan, num|
		// create new annonymous connection if none is given
		connection ?? {
			connection = MidiConnection(this);
		};
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};

		// pass execution to connection
		connection.midiConnect(src, chan, num);
	}

	midiDisconnect { |connection|
		connection ?? {
			Error("No connection given. Don't know which connection to disconnect!").throw;
		};
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
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
	addOscConnection { |name|
		name !? { name = name.asSymbol };
		^OscConnection(this, name);
	}

	removeOscConnection { |connection|
		if (connection.class == Symbol) {
			connection = oscConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};
		// FIXME: should call connection.remove instead?
		connection.oscDisconnect;
		oscConnections.remove(connection);
	}

	addMidiConnection { |name|
		name !? { name = name.asSymbol };
		^MidiConnection(this, name);
	}

	removeMidiConnection { |connection|
		if (connection.class == Symbol) {
			connection = midiConnections.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = midiConnections[connection]
		};
		// FIXME: should call connection.remove instead?
		connection.midiDisconnect;
		midiConnections.remove(connection);
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