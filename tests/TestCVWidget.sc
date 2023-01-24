TestCVWidget : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_globalSetup {
		var setup = CVWidget.globalSetup;
		this.assertEquals(setup.midiMode, 0, "CVWidget.midiMode should be 0 by default");
		this.assertEquals(setup.midiResolution, 1, "CVWidget.midiResolution should be 1 by default");
		this.assertEquals(setup.midiMean, 64, "CVWidget.midiMean should be 64 by default");
		this.assertEquals(setup.ctrlButtonBank, nil, "CVWidget.ctrlButtonBank should be nil by default");
		this.assertEquals(setup.softWithin, 0.1, "CVWidget.softWithin should be 0.1 by default");
	}

	test_syncKeys {
		this.assertEquals(widget.syncKeys, [\default], "Any new CVWidget instance should return an Array holding a key 'default' upon calling syncKeys");
	}

	test_extend {
		widget.extend(\test, { |c, w, m| widget.env.test = c.value }, [\cvSpec]);
		this.assertEquals(widget.syncKeys, [\default, \test], "Calling the widget's syncKeys method should return the default syncKeys amended by the key given in extend");
		widget.setSpec(\freq);
		this.assertEquals(widget.env.test, ControlSpec(20, 20000, 'exp', 0, 440, " Hz"), "The function given as second argument to 'extend' should have set the variable 'test_output' to a ControlSpec(20, 20000, 'exp', 0, 440, \" Hz\")");
		widget.env.test = nil;
		widget.addAction(\test, {});
		this.assertEquals(widget.env.test, nil, "As extend has only amended 'cvSpec' model widget.env.test should be nil");
	}

	test_reduce {
		widget.extend(\test, { |c, w, m| widget.env.test = c.value }, [\cvSpec], true);
		widget.reduce(\test);
		this.assertEquals(widget.syncKeys, [\default, \test], "After calling 'reduce' with a key \\test the widget's 'syncKeys' method should return an Array [\default, \test] as 'extend' has been called with the argument 'proto' set to true");
		widget.reduce(\test, true);
		this.assertEquals(widget.syncKeys, [\default], "After calling 'reduce' with the argument 'proto' set tu true the widget's 'syncKeys' method should return an Array [\default]");
	}
}


TestCVWidgetKnob : UnitTest {
	var widget, midiConnection, oscConnection;
	var connection1, connection2;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

/*	test_new {
		var oscConnection, midiConnection;
		this.assertEquals(widget.class, CVWidgetKnob, "A new CVWidgetKnob should identify itself as a CVWidgetKnob");
		this.assertEquals(widget.cv.class, CV, "A new CVWidgetKnob for which no CV has been specified should automatically have been created with a new CV");
		this.assertEquals(widget.cv.spec, \unipolar.asSpec, "A new CVWidgetKnob's CV should equal \unipolar.asSpec");
		this.assertEquals(widget.syncKeys, [\default], "A new CVWidgetKnob should initialize the syncKeys array with a single key \default");
		this.assertEquals(widget.wmc.class, Event, "A new CVWidgetKnob should initialize an Event kept in a variable named wmc");
		this.assertEquals(widget.wmc.keys, Set[\oscConnections, \cvSpec, \actions, \midiOptions, \oscDisplay, \oscInputRange, \midiDisplay, \oscCalibration, \midiConnections], "A CVWidgetKnob's wmc variable (an Event) should by default hold the following keys: \\oscConnectors, \\cvSpec, \\actions, \\midiOptions, \\oscDisplay, \\oscInputRange, \\midiDisplay, \\oscCalibration, \\midiConnectors");
		this.assertEquals(widget.oscConnectors.size, 1, "A new CVWidgetKnob should hold one OsConnection in 'oscConnectors'");
		this.assertEquals(widget.oscConnectors[0].name, 'OSC Connection 1', "The default OscConnector should be named 'OSC Connection 1'");
		this.assertEquals(widget.midiConnectors.size, 1, "A new CVWidgetKnob should hold one MidiConnector in 'midiConnectors");
		this.assertEquals(widget.midiConnectors[0].name, 'MIDI Connection 1', "The default MidiConnector should be named 'Midi Connection 1'")
	}

	test_setSpec {
		var testSpec = ControlSpec(1.0, 25.0, \exp, 0.0, 12);
		widget.setSpec(testSpec);
		this.assertEquals(widget.cv.spec, testSpec, "The widget's CV should now hold a new ControlSpec equaling ControlSpec(1.0, 25.0, \exp, 0.0, 12)");
		widget.setSpec(\freq.asSpec);
		this.assertEquals(widget.cv.spec, \freq.asSpec, "The widget's CV should now hold a new ControlSpec equaling ControlSpec(20.0, 20000.0, \exp, 0.0, 440)");
	}

	test_getSpec {
		this.assertEquals(widget.getSpec, \unipolar.asSpec, "The widget should have returned a ControlSpec(0, 1, 'linear', 0.0, 0, \"\") on calling getSpec");
		this.assert(widget.getSpec === widget.cv.spec, "The widget's CV's' spec and the return value of widget.getSpec should be identical");
	}

	test_add_removeMidiConnector {
		this.assertEquals(widget.midiConnectors.size, 1, "widget.midiConnectors should by default contain one midiConnection after widget instantiation");
		connection1 = widget.addMidiConnector;
		this.assertEquals(widget.midiConnectors.size, 2, "widget.midiConnectors should contain two midiConnectors after calling widget.addMidiConnector");
		this.assertEquals(connection1.name, 'MIDI Connection 2', "The anonymously added MidiConnector should have been named 'MIDI Connection 2'");
		connection2 = widget.addMidiConnector(\test);
		this.assertEquals(widget.midiConnectors.size, 3, "widget.midiConnectors should contain three midiConnectors after calling widget.addMidiConnector");
		this.assertEquals(connection2.name, \test, "The added MidiConnector should have been named 'test'");
		widget.removeMidiConnector(connection1);
		this.assertEquals(widget.midiConnectors.size, 2, "widget.midiConnectors should contain two midiConnectors after removing connection1");
		this.assertEquals(widget.midiConnectors.collect(_.name), ['MIDI Connection 1', \test], "widget.midiConnectors should contain two midiConnectors, named 'MIDI Connection 1' and 'test'");
		widget.removeMidiConnector(\test);
		this.assertEquals(widget.midiConnectors.size, 1, "widget.midiConnectors should contain one MidiConnector");
	}

	test_set_getMidiMode {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getMidiMode, [0, 0, 0], "All widget.midiConnectors should be set to midiMode 0 by default");
		widget.setMidiMode(1);
		this.assertEquals(widget.getMidiMode, [1, 1, 1], "All widget.midiConnectors should have been set to midiMode 1");
		widget.setMidiMode(0, connection1);
		this.assertEquals(widget.getMidiMode, [1, 0, 1], "widget.midiConnectors midiMode should equal [1, 0, 1]");
		widget.setMidiMode(0, 'MIDI Connection 1');
		this.assertEquals(widget.getMidiMode, [0, 0, 1], "widget.midiConnectors midiMode should equal [0, 0, 1]");
		widget.setMidiMode(1, 1);
		this.assertEquals(widget.getMidiMode, [0, 1, 1], "widget.midiConnectors midiMode should equal [0, 1, 1]")
	}

	test_set_getMidiMean {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getMidiMean, [64, 64, 64], "All widget.midiConnectors should be set to midiMean 64 by default");
		widget.setMidiMean(0);
		this.assertEquals(widget.getMidiMean, [0, 0, 0], "All widget.midiConnectors should have been set to midiMean 0");
		widget.setMidiMean(64, connection1);
		this.assertEquals(widget.getMidiMean, [0, 64, 0], "widget.midiConnectors midiMean should equal [64, 0, 0]");
		widget.setMidiMean(64, 'MIDI Connection 1');
		this.assertEquals(widget.getMidiMean, [64, 64, 0], "widget.midiConnectors midiMean should equal [64, 64, 0]");
		widget.setMidiMean(64, 2);
		this.assertEquals(widget.getMidiMean, [64, 64, 64], "widget.midiConnectors midiMean should equal [64, 64, 64]");
	}

	test_set_getSoftWithin {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getSoftWithin, [0.1, 0.1, 0.1], "All widget.midiConnectors should be set to softWithin 0.1 by default");
		widget.setSoftWithin(0.5);
		this.assertEquals(widget.getSoftWithin, [0.5, 0.5, 0.5], "All widget.midiConnectors should have been set to softWithin 0.5");
		widget.setSoftWithin(0.1, connection1);
		this.assertEquals(widget.getSoftWithin, [0.5, 0.1, 0.5], "widget.midiConnectors softWithin should equal [0.5, 0.1, 0.5]");
		widget.setSoftWithin(0.1, 'MIDI Connection 1');
		this.assertEquals(widget.getSoftWithin, [0.1, 0.1, 0.5], "widget.midiConnectors softWithin should equal [0.1, 0.1, 0.5]");
		widget.setSoftWithin(0.5, 0);
		this.assertEquals(widget.getSoftWithin, [0.5, 0.1, 0.5], "widget.midiConnectors softWithin should equal [0.5, 0.1, 0.5]");
	}*/

	test_set_getCtrlButtonBank {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getCtrlButtonBank, [nil, nil, nil], "All widget.midiConnectors should be set to ctrlButtonBank nil by default");
		widget.setCtrlButtonBank(16);
		this.assertEquals(widget.getCtrlButtonBank, [16, 16, 16], "All widget.midiConnectors should have been set to ctrlButtonBank 16");
		widget.setCtrlButtonBank(nil, connection1);
		this.assertEquals(widget.getCtrlButtonBank, [16, nil, 16], "widget.midiConnectors ctrlButtonBank should equal [16, nil, 16]");
		widget.setCtrlButtonBank(nil, 'MIDI Connection 1');
		this.assertEquals(widget.getCtrlButtonBank, [nil, nil, 16], "widget.midiConnectors ctrlButtonBank should equal [nil, nil, 16]");
		widget.setCtrlButtonBank(16, 1);
		this.assertEquals(widget.getCtrlButtonBank, [nil, 16, 16], "widget.midiConnectors ctrlButtonBank should equal [nil, 16, 16]");
	}

	/*test_set_getMidiResolution {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getMidiResolution, [1, 1, 1], "All widget.midiConnectors should be set to midiResolution 1 by default");
		widget.setMidiResolution(0.5);
		this.assertEquals(widget.getMidiResolution, [0.5, 0.5, 0.5], "All widget.midiConnectors should have been set to midiResolution 0.5");
		widget.setMidiResolution(1, connection1);
		this.assertEquals(widget.getMidiResolution, [0.5, 1, 0.5], "widget.midiConnectors midiResolution should equal [0.5, 1, 0.5]");
		widget.setMidiResolution(1, 'MIDI Connection 1');
		this.assertEquals(widget.getMidiResolution, [1, 1, 0.5], "widget.midiConnectors midiResolution should equal [1, 1, 0.5]");
		widget.setMidiResolution(1, 2);
		this.assertEquals(widget.getMidiResolution, [1, 1, 1], "widget.midiConnectors midiResolution should equal [1, 1, 1]");
	}

	test_midiConnect {
		var numConnections;
		MIDIIn.connectAll;
		numConnections = widget.midiConnectors.size;
		widget.midiConnect(0, num: 1);
		this.assert(numConnections == widget.midiConnectors.size, "The number of widget.midiConnectors should not have been increased after connecting the widget using the default midiConnection");
		this.assertEquals(widget.wmc.midiConnections.model[0].value, (num: 1), "After calling widget.midiConnect(0, num: 1) widget.wmc.midiConnection.model[0].value should equal (num: 1)");
		widget.midiConnect(num: 2);
		this.assert(widget.midiConnectors.size == (numConnections + 1) , "The number of widget.midiConnectors should not have been increased by 1 after connecting the widget without specifying a midiConnection");
		this.assertEquals(widget.wmc.midiConnections.model[1].value, (num: 2), "After calling widget.midiConnect(0, num: 1) widget.wmc.midiConnection.model[1].value should equal (num: 2)");
		// midi learn
		widget.midiConnect;
		MIDIIn.doControlAction(12345, 0, 5, 127);
		this.assertEquals(widget.wmc.midiConnections.model[2].value, (src: 12345, chan: 0, num: 5), "After calling widget.midiConnect widget.wmc.midiConnection.model[2].value should have set by 'learning' to' (src: 12345, chan: 0, num: 5)");
	}

	test_midiDisconnect {
		widget.midiConnect(0, num: 1);
		this.assertEquals(widget.wmc.midiConnections.model[0].value, (num: 1), "After calling widget.midiConnect(0, num: 1) widget.wmc.midi[0].midiConnection.model.value should equal (num: 1)");
		widget.midiDisconnect(0);
		this.assertEquals(widget.wmc.midiConnections.model[0].value, nil, "After calling widget.midiDisonnect(0) widget.wmc.midi[0].midiConnection.model.value should equal nil");
		widget.midiConnect(0, num: 1);
		this.assertEquals(widget.wmc.midiConnections.model[0].value, (num: 1), "After calling widgetDisConnect(0) and calling widget.midiConnect(0, num: 1) again widget.wmc.midi[0].midiConnection.model.value should equal (num: 1)");
	}

	test_setOscCalibration {}

	test_getOscCalibration {}

	test_setOscMapping {}

	test_getOscMapping {}

	test_setOscInputConstraints {}

	test_getOscInputConstraints {}

	test_oscConnect {}

	test_oscDisconnect {}

	test_addAction {
		widget.addAction("active", { |cv, wdgt| wdgt.env.res1_([cv.value, wdgt.name]) }, true);
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 1, activeActions: 1), "The widget should hold one action and one marked as active after calling addAction with arg 'active' set to true");
		this.assertEquals(widget.widgetActions[\active].key.class, SimpleController, "The widget.widgetActions should hold a SimpleController as key at key 'active'");
		widget.addAction(\inactive, { |cv, wdgt| wdgt.env.res2_(nil) }, false);
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 2, activeActions: 1), "The widget should hold two actions and one marked as active after calling addAction with arg 'active' set to false");
		this.assertEquals(widget.widgetActions[\inactive].key, nil, "The widget.widgetActions should hold a SimpleController as key at key 'inactive'");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res1], [0.5, \test], "The result of the evaluation of the custom action 'active' should be [0.5, 'test'] after setting the widgets cv's value");
		widget.addAction(\stringAction, "{ |cv, wdgt| wdgt.env.res3_([cv.value, wdgt.name]) }", true);
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 3, activeActions: 2), "The widget should hold three actions and two marked as active after calling addAction with arg 'active' set to true");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res3], [0.5, \test], "The result of the evaluation of the custom action 'stringAction' should be [0.5, 'test'] after setting the widgets cv's value");
	}

	test_activateAction {
		widget.addAction(\inactive, { |cv, wdgt| wdgt.env.res1_([cv.value, wdgt.name]) }, false);
		widget.activateAction(\inactive, true);
		this.assertEquals(widget.widgetActions[\inactive].key.class, SimpleController, "The widget.widgetActions should hold a SimpleController as key at key 'inactive' after calling activateAction");
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 1, activeActions: 1), "The widget should hold one action and one marked as active after calling activateAction with arg 'active' set to true");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res1], [0.5, \test], "The result of the evaluation of the custom action 'inactive' should be [0.5, 'test'] after setting the widgets cv's value");
		widget.activateAction(\inactive, false);
		this.assertEquals(widget.widgetActions[\inactive].key, nil, "The widget.widgetActions should hold nil as key at key 'inactive' after calling activateAction with arg 'active' set to false");
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 1, activeActions: 0), "The widget should hold one action and one marked as inactive after calling activateAction with arg 'active' set to false");
	}

	test_removeAction {
		widget.addAction("active", {}, true);
		widget.addAction(\inactive, {}, false);
		widget.addAction(\stringAction, "{}", true);
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 3, activeActions: 2), "The widget should hold three actions and two of them should be marked as active");
		widget.removeAction(\active);
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 2, activeActions: 1), "The widget should hold two actions and one of them should be marked as active after removing the action 'active'");
		widget.removeAction(\inactive);
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 1, activeActions: 1), "The widget should hold one action and one should be marked as active after removing the action 'inactive'");
		widget.removeAction(\stringAction);
		this.assertEquals(widget.wmc.actions.model.value, (numActions: 0, activeActions: 0), "The widget should hold no actions after removing the action 'stringAction'");
	}

	test_updateAction {
		widget.addAction("active", { |cv, wdgt| wdgt.env.res1_([cv.value, wdgt.name]) }, true);
		widget.cv.value_(0.5);
		this.assertEquals(widget.env.res1, [0.5, \test], "widget.env.res1 should equal [0.5, \test] after setting the widget cv's value");
		widget.updateAction(\active, { |cv, wdgt| wdgt.env.res1 = [cv.value, wdgt.getSpec] });
		widget.cv.value_(0);
		this.assertEquals(widget.env.res1, [0.0, ControlSpec(0, 1, 'linear', 0.0, 0.0, "")], "widget.env.res1 should equal [0.0, ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "")] after having updated the action and setting the widget cv's value to 0");
		widget.updateAction(\active, "{ |cv, wdgt| wdgt.env.res1_([cv.value, wdgt.name]) }");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env.res1, [0.5, \test], "widget.env.res1 should equal [0.5, 'test'] after having updated the action and setting the widget cv's value");
	}*/
}