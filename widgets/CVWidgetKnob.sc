CVWidgetKnob : AbstractCVWidget {
	var <name, <cv;
	var <wmc; //widget models and controllers

	var <syncKeys, syncedActions;
	var <oscConnections, <midiConnections;

	*new { |name, cv, setup, action|
		^super.newCopyArgs(name, cv).init(setup, action);
	}

	init { |setupArgs, action|
		name ?? {
			Error("No name provided for new CVWidgetKnob").throw;
		};

		cv ?? { cv = CV.new };
		syncKeys ?? { syncKeys = [\default] };

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

		this.initModels;
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

		// each OSC/MIDI connection needs its own model
		wmc.osc ?? { wmc.osc = List[] };
		wmc.midi ?? { wmc.midi = List[] };

		this.initControllers;
	}

	initControllers {
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
		wmc.cvSpec.model.value_(spec).changedKeys(syncKeys);
	}

	getSpec {
		^cv.spec;
	}

	// CV actions
	addAction {}
	removeAction {}
	activateAction {}

	// MIDI
	setMidiMode { |mode, connection|
		// connection can be a Symbol or a MidiConnection instance
		if (connection.class == Symbol) {
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
			connection = midiConnections[connection];
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
			connection = midiConnections[connection];
		};

		if (connection.isNil) {
			^midiConnections.collect(_.getMidiMean)
		} {
			^connection.getMidiMean;
		}
	}

	setSoftWithin { |threshold, connection|
		if (connection.class == Symbol) {
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
			connection = midiConnections[connection]
		};

		if (connection.isNil) {
			^midiConnections.collect(_.getMidiResolution);
		} {
			^connection.getMidiResolution;
		}
	}

	midiConnect { |connection, uid, chan, num|
		// create new annonymous connection if none is given
		connection ?? {
			connection = MidiConnection(this);
		};
		if (connection.class == Symbol) {
			connection = midiConnections[connection];
		};

		// pass execution to connection
		connection.midiConnect(uid, chan, num);
	}

	midiDisconnect { |connection|
		connection ?? {
			Error("No connection given. Don't know which connection to disconnect!");
		};
		if (connection.class == Symbol) {
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
	// init controllers (private)
	prInitSpecControl {
		var controller = wmc.cvSpec.controller;
		controller ?? {
			controller = SimpleController(wmc.cvSpec.model);
		};
		controller.put(\default, { |changer, what, moreArgs|
			cv.spec_(changer.value);
		})
	}

	prInitActionsControl {}
}