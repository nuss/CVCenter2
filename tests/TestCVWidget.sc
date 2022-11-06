TestCVWidget : UnitTest {
	var widget, test_output;

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
		widget.extend(\test, { |c, w, m| test_output = c.value }, [\cvSpec]);
		this.assertEquals(widget.syncKeys, [\default, \test], "Calling the widget's syncKeys method should return the default syncKeys amended by the key given in extend");
		widget.setSpec(\freq);
		this.assertEquals(test_output, ControlSpec(20, 20000, 'exp', 0, 440, " Hz"), "The function given as second argument to 'extend' should have set the variable 'test_output' to a ControlSpec(20, 20000, 'exp', 0, 440, \" Hz\")");
	}

	test_reduce {
		widget.reduce(\test);
		this.assertEquals(widget.syncKeys, [\default], "After calling 'reduce' with a key \\test the widget's 'syncKeys' method should return an Array [\default]");
		widget.extend(\test, { |c, w, m| test_output = c.value }, [\cvSpec], true);
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

	test_new {
		var oscConnection, midiConnection;
		this.assertEquals(widget.class, CVWidgetKnob, "A new CVWidgetKnob should identify itself as a CVWidgetKnob");
		this.assertEquals(widget.cv.class, CV, "A new CVWidgetKnob for which no CV has been specified should automatically have been created with a new CV");
		this.assertEquals(widget.cv.spec, \unipolar.asSpec, "A new CVWidgetKnob's CV should equal \unipolar.asSpec");
		this.assertEquals(widget.syncKeys, [\default], "A new CVWidgetKnob should initialize the syncKeys array with a single key \default");
		this.assertEquals(widget.wmc.class, Event, "A new CVWidgetKnob should initialize an Event kept in a variable named wmc");
		this.assertEquals(widget.wmc.keys, Set[\cvSpec, \actions, \osc, \midi], "A CVWidgetKnob's wmc variable (an Event) should by default hold 4 keys: \cvSpec, \actions, \osc, \midi");
		this.assertEquals(widget.oscConnections.size, 1, "A new CVWidgetKnob should hold one OsConnection in 'oscConnections'");
		this.assertEquals(widget.oscConnections[0].name, 'OSC Connection 1', "The default OscConnection should be named 'OSC Connection 1'");
		this.assertEquals(widget.midiConnections.size, 1, "A new CVWidgetKnob should hold one MidiConnection in 'midiConnections");
		this.assertEquals(widget.midiConnections[0].name, 'MIDI Connection 1', "The default MidiConnection should be named 'Midi Connection 1'")
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

	test_add_removeMidiConnection {
		this.assertEquals(widget.midiConnections.size, 1, "widget.midiConnections should by default contain one midiConnection after widget instantiation");
		connection1 = widget.addMidiConnection;
		this.assertEquals(widget.midiConnections.size, 2, "widget.midiConnections should contain two midiConnections after calling widget.addMidiConnection");
		this.assertEquals(connection1.name, 'MIDI Connection 2', "The anonymously added MidiConnection should have been named 'MIDI Connection 2'");
		connection2 = widget.addMidiConnection(\test);
		this.assertEquals(widget.midiConnections.size, 3, "widget.midiConnections should contain three midiConnections after calling widget.addMidiConnection");
		this.assertEquals(connection2.name, \test, "The added MidiConnection should have been named 'test'");
		widget.removeMidiConnection(connection1);
		this.assertEquals(widget.midiConnections.size, 2, "widget.midiConnections should contain two midiConnections after removing connection1");
		this.assertEquals(widget.midiConnections.collect(_.name), ['MIDI Connection 1', \test], "widget.midiConnections should contain two midiConnections, named 'MIDI Connection 1' and 'test'");
		widget.removeMidiConnection(\test);
		this.assertEquals(widget.midiConnections.size, 1, "widget.midiConnections should contain one MidiConnection");
	}

	test_set_getMidiMode {
		connection1 = widget.addMidiConnection;
		connection2 = widget.addMidiConnection;
		this.assertEquals(widget.getMidiMode, [0, 0, 0], "All widget.midiConnections should be set to midiMode 0 by default");
		widget.setMidiMode(1);
		this.assertEquals(widget.getMidiMode, [1, 1, 1], "All widget.midiConnections should have been set to midiMode 1");
		widget.setMidiMode(0, connection1);
		this.assertEquals(widget.getMidiMode, [1, 0, 1], "widget.midiConnections midiMode should equal [1, 0, 1]");
		widget.setMidiMode(0, 'MIDI Connection 1');
		this.assertEquals(widget.getMidiMode, [0, 0, 1], "widget.midiConnections midiMode should equal [0, 0, 1]");
	}

	test_set_getMidiMean {
		connection1 = widget.addMidiConnection;
		connection2 = widget.addMidiConnection;
		this.assertEquals(widget.getMidiMean, [64, 64, 64], "All widget.midiConnections should be set to midiMean 64 by default");
		widget.setMidiMean(0);
		this.assertEquals(widget.getMidiMean, [0, 0, 0], "All widget.midiConnections should have been set to midiMean 0");
		widget.setMidiMean(64, connection1);
		this.assertEquals(widget.getMidiMean, [0, 64, 0], "widget.midiConnections midiMean should equal [64, 0, 0]");
		widget.setMidiMean(64, 'MIDI Connection 1');
		this.assertEquals(widget.getMidiMean, [64, 64, 0], "widget.midiConnections midiMean should equal [64, 64, 0]");
	}

	test_set_getSoftWithin {
		connection1 = widget.addMidiConnection;
		connection2 = widget.addMidiConnection;
		this.assertEquals(widget.getSoftWithin, [0.1, 0.1, 0.1], "All widget.midiConnections should be set to softWithin 0.1 by default");
		widget.setSoftWithin(0.5);
		this.assertEquals(widget.getSoftWithin, [0.5, 0.5, 0.5], "All widget.midiConnections should have been set to softWithin 0.5");
		widget.setSoftWithin(0.1, connection1);
		this.assertEquals(widget.getSoftWithin, [0.5, 0.1, 0.5], "widget.midiConnections softWithin should equal [0.5, 0.1, 0.5]");
		widget.setSoftWithin(0.1, 'MIDI Connection 1');
		this.assertEquals(widget.getSoftWithin, [0.1, 0.1, 0.5], "widget.midiConnections softWithin should equal [0.1, 0.1, 0.5]");
	}

	test_set_getCtrlButtonBank {
		connection1 = widget.addMidiConnection;
		connection2 = widget.addMidiConnection;
		this.assertEquals(widget.getCtrlButtonBank, [nil, nil, nil], "All widget.midiConnections should be set to ctrlButtonBank nil by default");
		widget.setCtrlButtonBank(16);
		this.assertEquals(widget.getCtrlButtonBank, [16, 16, 16], "All widget.midiConnections should have been set to ctrlButtonBank 16");
		widget.setCtrlButtonBank(nil, connection1);
		this.assertEquals(widget.getCtrlButtonBank, [16, nil, 16], "widget.midiConnections ctrlButtonBank should equal [16, nil, 16]");
		widget.setCtrlButtonBank(nil, 'MIDI Connection 1');
		this.assertEquals(widget.getCtrlButtonBank, [nil, nil, 16], "widget.midiConnections ctrlButtonBank should equal [nil, nil, 16]");
	}

	test_set_getMidiResolution {
		connection1 = widget.addMidiConnection;
		connection2 = widget.addMidiConnection;
		this.assertEquals(widget.getMidiResolution, [1, 1, 1], "All widget.midiConnections should be set to midiResolution 1 by default");
		widget.setMidiResolution(0.5);
		this.assertEquals(widget.getMidiResolution, [0.5, 0.5, 0.5], "All widget.midiConnections should have been set to midiResolution 0.5");
		widget.setMidiResolution(1, connection1);
		this.assertEquals(widget.getMidiResolution, [0.5, 1, 0.5], "widget.midiConnections midiResolution should equal [0.5, 1, 0.5]");
		widget.setMidiResolution(1, 'MIDI Connection 1');
		this.assertEquals(widget.getMidiResolution, [1, 1, 0.5], "widget.midiConnections midiResolution should equal [1, 1, 0.5]");
	}

	test_midiConnect {}

	test_midiDisconnect {}

	test_removeMidiConnection {

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
		this.assertEquals(widget.widgetActions[\active].key.class, SimpleController, "The widget.widgetActions should hold a SimpleController as key at key 'active'");
		widget.addAction(\inactive, { |cv, wdgt| wdgt.env.res2_(nil) }, false);
		this.assertEquals(widget.widgetActions[\inactive].key, nil, "The widget.widgetActions should hold a SimpleController as key at key 'inactive'");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res1], [0.5, \test], "The result of the evaluation of the custom action 'active' should be [0.5, 'test'] after setting the widgets cv's value");
		widget.addAction(\stringAction, "{ |cv, wdgt| wdgt.env.res3_([cv.value, wdgt.name]) }", true);
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res3], [0.5, \test], "The result of the evaluation of the custom action 'stringAction' should be [0.5, 'test'] after setting the widgets cv's value");
	}

	test_activateAction {
		widget.addAction(\inactive, { |cv, wdgt| wdgt.env.res1_([cv.value, wdgt.name]) }, false);
		widget.activateAction(\inactive, true);
		this.assertEquals(widget.widgetActions[\inactive].key.class, SimpleController, "The widget.widgetActions should hold a SimpleController as key at key 'inactive' after calling activateAction");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res1], [0.5, \test], "The result of the evaluation of the custom action 'inactive' should be [0.5, 'test'] after setting the widgets cv's value");
	}
}