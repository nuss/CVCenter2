CVWidgetKnob : CVWidget {
	var /*<cv, */setup;
	// only needed for naming a connector
	var <>numOscConnectors = 0, <>numMidiConnectors = 0;

	*new { |name, cv, setup, action, modelsAndControllers|
		if (cv.size > 0) {
			"Cannot create new CVWidgetKnob from multichannel CV. Try CVWidgetMS.new instead.".error;
			^nil;
		};
		^super.newCopyArgs(name, cv: cv, setup: setup).init(action);
	}

	init { |action|
		name ?? {
			Error("No name provided for new CVWidgetKnob").throw;
		};
		name = name.asSymbol;

		if (all[name].isNil) { all.put(name, this) } {
			"A CVWidgetKnob under the given name '%' already exists. Please choose a differnet name.".format(name).error;
			^nil
		};


		this.cv ?? { cv = CV.new };

		syncKeysEvent ?? {
			syncKeysEvent = (proto: List[\default], user: List[])
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

		wmc = ();
		this.initModels(wmc);

		setup !? {
			setup.isKindOf(Dictionary).not.if {
				Error("A setup has to be provided as a Dictionary or an Event").throw
			};
			setup[\midiMode] !? { this.setMidiMode(setup[\midiMode]) };
			setup[\midiResolution] !? { this.setMidiResolution(setup[\midiResolution]) };
			setup[\midiMean] !? { this.setMidiZero(setup[\midiMean]) };
			setup[\midiCtrlButtonBank] !? { this.setMidiCtrlButtonGroup(setup[\midiCtrlButtonBank]) };
			setup[\midiSnapDistance] !? { this.setMidiSnapDistance(setup[\midiSnapDistance]) };
			setup[\midiInputMapping] !?	{ this.setMidiInputMapping(setup[\midiInputMapping]) };
			setup[\oscCalibration] !? { this.setOscCalibration(setup[\oscCalibration]) };
			setup[\oscInputRange] !? { this.setOscInputConstraints(setup[\oscInputRange]) };
			setup[\oscInputMapping] !? { this.setOscInputMapping(setup[\oscInputMapping]) };
			setup[\oscEndless] !? { this.setOscEndless(setup[\oscEndless]) };
			setup[\oscResolution] !? { this.setOscResolution(setup[\oscResolution]) };
			setup[\oscSnapDistance] !? { this.setOscSnapDistance(setup[\oscSnapDistance]) };
			setup[\oscMatching] !? { this.setOscMatching(setup[\oscMatching]) };
		}
	}

	initModels { |modelsControllers|
		// models, not tied to connectors, global to all
		// MIDI and OSC connections
		wmc.cvSpec = (m: Ref(this.cv.spec));
		wmc.actions = (m: Ref((numActions: 0, activeActions: 0)));
		wmc.midiConnectors = (m: Ref(List[]));
		wmc.oscConnectors = (m: Ref(List[]));

		this.initControllers(wmc);

		// every new CVWidget should
		// immediately be amended by
		// an empty OscConnector
		// resp. an empty MidiConnector
		// controllers for connectors
		// are added within these classes
		OscConnector(this);
		MidiConnector(this);
		all.changed;
	}

	midiConnectors {
		^wmc.midiConnectors.m.value
	}

	oscConnectors {
		^wmc.oscConnectors.m.value
	}

	// the CV's ControlSpec
	setSpec { |spec|
		if ((spec = spec.asSpec).isKindOf(ControlSpec).not) {
			Error("No valid ControlSpec given for setSpec.").throw;
		};
		wmc.cvSpec.m.value_(spec).changedPerformKeys(this.syncKeys);
	}

	//  common OSC and MIDI helpers
	getConnector { |connectorKind, connector|
		var connectors;

		if (connectorKind !== \midi and: { connectorKind !== \osc }) {
			Error("CVWidgetKnob:-getConnector: arg 'connectorKind' (first argument) must either be 'midi' or 'osc'.").throw
		}{
			connectors = switch(connectorKind)
			{ \midi } { this.midiConnectors }
			{ \osc } { this.oscConnectors };

			if (connector.isInteger) {
				^connectors[connector]
			};
			^connector
		}
	}

	// MIDI
	setMidiMode { |mode, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMidiMode(mode))
		} {
			connector.setMidiMode(mode)
		}
	}

	getMidiMode { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMidiMode);
		} {
			^connector.getMidiMode;
		}
	}

	setMidiZero { |zeroval, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMidiZero(zeroval))
		} {
			connector.setMidiZero(zeroval)
		}
	}

	getMidiZero { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMidiZero)
		} {
			^connector.getMidiZero;
		}
	}

	setMidiSnapDistance { |snapDistance, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMidiSnapDistance(snapDistance));
		} {
			connector.setMidiSnapDistance(snapDistance);
		}
	}

	getMidiSnapDistance { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMidiSnapDistance);
		} {
			^connector.getMidiSnapDistance;
		}
	}

	setMidiCtrlButtonGroup { |numButtons, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMidiCtrlButtonGroup(numButtons));
		} {
			connector.setMidiCtrlButtonGroup(numButtons);
		}
	}

	getMidiCtrlButtonGroup { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMidiCtrlButtonGroup);
		} {
			^connector.getMidiCtrlButtonGroup;
		}
	}

	setMidiResolution { |resolution, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMidiResolution(resolution));
		} {
			connector.setMidiResolution(resolution);
		}
	}

	getMidiResolution { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMidiResolution)
		} {
			^connector.getMidiResolution
		}
	}

	setMidiInputMapping { |mapping, curve, env, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMidiInputMapping(mapping, curve, env))
		} {
			connector.setMidiInputMapping(mapping, curve, env)
		}
	}

	getMidiInputMapping { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMidiInputMapping)
		} {
			^connector.getMidiInputMapping
		}
	}

	setMiditemplate { |argTemplate, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMidiTemplate(argTemplate))
		} {
			connector.setMidiTemplate(argTemplate)
		}
	}

	getMidiTemplate { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMidiTemplate)
		} {
			^connector.getMidiTemplate
		}
	}

	setMidiDispatcher { |dispatcher, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMidiDispatcher(dispatcher))
		} {
			connector.setMidiDispatcher(dispatcher)
		}
	}

	getMidiDispatcher { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMidiDispatcher)
		} {
			^connector.getMidiDispatcher
		}
	}

	setMIDIFuncEnabled { |boolEnabled, connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			this.midiConnectors.do(_.setMIDIFuncEnabled(boolEnabled))
		} {
			connector.setMIDIFuncEnabled(boolEnabled)
		}
	}

	getMIDIFuncEnabled { |connector|
		connector = this.getConnector(\midi, connector);
		if (connector.isNil) {
			^this.midiConnectors.collect(_.getMIDIFuncEnabled)
		} {
			^connector.getMIDIFuncEnabled
		}
	}



	midiConnect { |connector, src, chan, num, argTemplate, dispatcher|
		// create new annonymous connector if none is given
		connector ?? {
			if (this.midiConnectors.size == 1 and: {
				wmc.midiConnections.m.value[0].isNil
			}) {
				connector = this.midiConnectors[0]
			} {
				connector = MidiConnector(this)
			}
		};
		connector = this.getConnector(\midi, connector);
		// pass execution to connector
		connector.midiConnect(num, chan, src, argTemplate, dispatcher);
	}

	midiDisconnect { |connector|
		connector ?? {
			"CVWidgetMS:-midiDisconnect: No connector given. Don't know which connector to disconnect.".error;
			^nil
		};
		connector = this.getConnector(\midi, connector);
		// pass execution to connector
		connector.midiDisconnect
	}

	// OSC
	getOscConnector { |connector|
		if (connector.isInteger) {
			^this.oscConnectors[connector]
		};
		^connector
	}

	setOscEndless { |boolEndless, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscEndless(boolEndless))
		} {
			connector.setOscEndless(boolEndless)
		}
	}

	getOscEndless { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscEndless);
		} {
			^connector.getOscEndless;
		}
	}

	setOscResolution { |resolution, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscResolution(resolution))
		} {
			connector.setOscResolution(resolution)
		}
	}

	getOscResolution { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscResolution);
		} {
			^connector.getOscResolution;
		}
	}

	setOscSnapDistance { |distance, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscSnapDistance(distance))
		} {
			connector.setOscSnapDistance(distance)
		}
	}

	getOscSnapDistance { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscSnapDistance);
		} {
			^connector.getOscSnapDistance;
		}
	}

	setOscCalibration { |boolCalibration, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscCalibration(boolCalibration))
		} {
			connector.setOscCalibration(boolCalibration)
		}
	}

	getOscCalibration { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscCalibration)
		} {
			^connector.getOscCalibration;
		}
	}

	resetOscCalibration { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.resetOscCalibration)
		} {
			connector.resetOscCalibration
		}
	}

	setOscInputMapping { |mapping, curve, env, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscInputMapping(mapping, curve, env))
		} {
			connector.setOscInputMapping(mapping, curve, env)
		}
	}

	getOscInputMapping { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscInputMapping);
		} {
			^connector.getOscInputMapping;
		}
	}

	setOscInputConstraints { |constraintsPair, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscInputConstraints(constraintsPair))
		} {
			connector.setOscInputConstraints(constraintsPair)
		}
	}

	getOscInputConstraints { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscInputConstraints);
		} {
			^connector.getOscInputConstraints;
		}
	}

	setOscMatching { |boolMatching, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscMatching(boolMatching))
		} {
			connector.setOscMatching(boolMatching)
		}
	}

	getOscMatching { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscMatching);
		} {
			^connector.getOscMatching;
		}
	}

	setOscInputAlwaysPositive { |value, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscInputAlwaysPositive(value))
		} {
			connector.setOscInputAlwaysPositive(value)
		}
	}

	getOscInputAlwaysPositive { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscInputAlwaysPositive);
		} {
			^connector.getOscInputAlwaysPositive;
		}
	}

	setOscCmdName { |cmdPath, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscCmdName(cmdPath))
		} {
			connector.setOscCmdName(cmdPath)
		}
	}

	getOscCmdName { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscCmdName);
		} {
			^connector.getOscCmdName;
		}
	}

	setOscMsgIndex { |msgIndex, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscMsgIndex(msgIndex))
		} {
			connector.setOscMsgIndex(msgIndex)
		}
	}

	getOscMsgIndex { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscMsgIndex);
		} {
			^connector.getOscMsgIndex;
		}
	}

	setOscTemplate { |argTemplate, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscTemplate(argTemplate))
		} {
			connector.setOscTemplate(argTemplate)
		}
	}

	getOscTemplate { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscTemplate)
		} {
			^connector.getOscTemplate
		}
	}

	setOscDispatcher { |dispatcher, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscDispatcher(dispatcher))
		} {
			connector.setOscDispatcher(dispatcher)
		}
	}

	getOscDispatcher { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscDispatcher)
		} {
			^connector.getOscDispatcher
		}
	}

	setOSCFuncEnabled { |boolEnabled, connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOSCFuncEnabled(boolEnabled))
		} {
			connector.setOSCFuncEnabled(boolEnabled)
		}
	}

	getOSCFuncEnabled { |connector|
		connector = this.getConnector(\osc, connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOSCFuncEnabled)
		} {
			^connector.getOSCFuncEnabled
		}
	}

	oscConnect { |connector, addr, cmdPath, oscMsgIndex = 1, recvPort, argTemplate, dispatcher, matching = false|
		// create new annonymous connector if none is given
		connector ?? {
			if (this.oscConnectors.size == 1 and: {
				wmc.oscConnections.m.value[0].isNil
			}) {
				connector = this.oscConnectors[0]
			} {
				connector = OscConnector(this)
			}
		};
		connector = this.getConnector(\osc, connector);
		// pass execution to connector
		connector.oscConnect(addr, cmdPath, oscMsgIndex, recvPort, argTemplate, dispatcher, matching);
	}

	oscDisconnect { |connector|
		connector ?? {
			Error("No connector given. Don't know which connector to disconnect.").throw;
		};
		connector = this.getConnector(\osc, connector);
		// pass execution to connector
		connector.oscDisconnect
	}

	// connections handling
	addOscConnector { |name|
		name !? { name = name.asSymbol };
		^OscConnector(this, name);
	}

	removeOscConnector { |connector, forceAll = false|
		if (connector.isInteger) {
			connector = this.midiConnectors[connector]
		};
		connector.remove(forceAll);
	}

	addMidiConnector { |name|
		name !? { name = name.asSymbol };
		^MidiConnector(this, name);
	}

	removeMidiConnector { |connector, forceAll = false|
		if (connector.isInteger) {
			connector = this.midiConnectors[connector]
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
		this.wmc.do { |val|
			if (val.class === Event) { val.c.remove };
			if (val.class === List) { val.do { |it|
				it.pairsDo { |k, v| v.release };
			}}
		};
		all.removeAt(name);
	}

	// // init controllers (private)
	// prInitSpecControl { |wmc, cv|
	// 	wmc.cvSpec.c ?? {
	// 		wmc.cvSpec.c = SimpleController(wmc.cvSpec.m);
	// 	};
	// 	wmc.cvSpec.c.put(\default, { |changer, what, moreArgs|
	// 		this.cv.spec_(changer.value);
	// 	})
	// }
	//
	// prInitActionsControl { |wmc, cv|
	// 	wmc.actions.c ?? {
	// 		wmc.actions.c = SimpleController(wmc.actions.m);
	// 	};
	// 	wmc.actions.c.put(\default, { |changer, what, moreArgs|
	// 		// do something with changer.value
	// 	})
	// }

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