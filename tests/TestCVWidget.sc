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
		this.assertEquals(setup.midiZero, 64, "CVWidget.midiZero should be 64 by default");
		this.assertEquals(setup.ctrlButtonGroup, 1, "CVWidget.ctrlButtonGroup should be 1 by default");
		this.assertEquals(setup.snapDistance, 0.1, "CVWidget.snapDistance should be 0.1 by default");
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

	test_new {
		var oscConnection, midiConnection;
		this.assertEquals(widget.class, CVWidgetKnob, "A new CVWidgetKnob should identify itself as a CVWidgetKnob");
		this.assertEquals(widget.cv.class, CV, "A new CVWidgetKnob for which no CV has been specified should automatically have been created with a new CV");
		this.assertEquals(widget.cv.spec, \unipolar.asSpec, "A new CVWidgetKnob's CV should equal \unipolar.asSpec");
		this.assertEquals(widget.syncKeys, [\default], "A new CVWidgetKnob should initialize the syncKeys array with a single key \default");
		this.assertEquals(widget.wmc.class, Event, "A new CVWidgetKnob should initialize an Event kept in a variable named wmc");
		this.assertEquals(widget.wmc.keys, Set[
			'midiConnectors',
			'oscConnectors',
			'midiOptions',
			'oscCalibration',
			'oscInputRange',
			'midiInputMappings',
			'midiDisplay',
			'actions',
			'oscDisplay',
			'oscConnectorNames',
			'midiConnections',
			'oscConnections',
			'cvSpec',
			'midiConnectorNames'
		], "A CVWidgetKnob's wmc variable (an Event) should by default hold expected keys");
		this.assertEquals(widget.wmc.oscConnectors.m.value.size, 1, "A new CVWidgetKnob should hold one OsConnection in 'oscConnectors'");
		this.assertEquals(widget.wmc.oscConnectors.m.value[0].name, 'OSC Connection 1', "The default OscConnector should be named 'OSC Connection 1'");
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 1, "A new CVWidgetKnob should hold one MidiConnector in 'midiConnectors");
		this.assertEquals(widget.wmc.midiConnectors.m.value[0].name, 'MIDI Connection 1', "The default MidiConnector should be named 'Midi Connection 1'")
	}

	test_add_remove_MidiConnector {
		var connector = widget.addMidiConnector(\midiConnector2);
		this.assertEquals(widget.wmc.midiConnectors.m.value.collect(_.name), ['MIDI Connection 1', 'midiConnector2'], "After adding a MididConnector widget.wmc.midiConnectors.m.value should hold 2 MidiConnectors named 'MIDI Connection 1' and 'midiConnector2'");
		widget.removeMidiConnector(0);
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 1, "After removing the MidiConnector at index 0 widget.wmc.midiConnectors.m.value should hold one MidiConnector");
		this.assertEquals(widget.wmc.midiConnectors.m.value[0].name, \midiConnector2, "The remaining MidiConnector stored in widget.wmc.midiConnectors.m.value after calling widget.removeMidiConnector(0) should be named 'midiConnector2'");
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
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 1, "widget.wmc.midiConnectors.m.value should by default contain one MidiConnector after widget instantiation");
		this.assertEquals(Set[widget.wmc.midiConnectors.m.value.size], Set[
			widget.getMidiMode.size,
			widget.getMidiZero.size,
			widget.getSnapDistance.size,
			widget.getCtrlButtonGroup.size,
			widget.getMidiResolution.size
		], "(1) The number of MidiConnectors should equal the size of the array returned by widget.getMidiMode");
		this.assertEquals(Set[widget.wmc.midiConnectors.m.value.size], Set[
			widget.wmc.midiDisplay.m.value.size,
			widget.wmc.midiOptions.m.value.size,
			widget.wmc.midiConnections.m.value.size,
			widget.wmc.midiConnectorNames.m.value.size
		], "(1) The number of MidiConnectors should equal the size of the widget's midiOptions, midiDisplay, midiConnections, midiConnectorNames model value arrays");
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, widget.wmc.midiConnections.m.value.size, "The number of midiConnectors should equal the size of the widget's midiConnections model array: 1");
		connection1 = widget.addMidiConnector;
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 2, "widget.wmc.midiConnectors.m.value should contain two midiConnectors after calling widget.addMidiConnector");
		this.assertEquals(connection1.name, 'MIDI Connection 2', "The anonymously added MidiConnector should have been named 'MIDI Connection 2'");
		this.assertEquals(Set[widget.wmc.midiConnectors.m.value.size], Set[
			widget.getMidiMode.size,
			widget.getMidiZero.size,
			widget.getSnapDistance.size,
			widget.getCtrlButtonGroup.size,
			widget.getMidiResolution.size
		], "(2) The number of midiConnectors should equal the size of the array returned by widget.getMidiMode");
		this.assertEquals(Set[widget.wmc.midiConnectors.m.value.size], Set[
			widget.wmc.midiDisplay.m.value.size,
			widget.wmc.midiOptions.m.value.size,
			widget.wmc.midiConnections.m.value.size,
			widget.wmc.midiConnectorNames.m.value.size
		], "(2) The number of midiConnectors should equal the size of the widget's midiDisplay, midiOptions, midiConnections, midiConnectorNames model value arrays");
		connection2 = widget.addMidiConnector(\test);
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 3, "widget.wmc.midiConnectors.m.value should contain three midiConnectors after calling widget.addMidiConnector");
		this.assertEquals(connection2.name, \test, "The added MidiConnector should have been named 'test'");
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, widget.getMidiMode.size, "The number of the widget's midiConnectors and the size of the array returned by widget.getMidiMode should equal: 3");
		this.assertEquals(Set[widget.wmc.midiConnectors.m.value.size], Set[
			widget.getMidiMode.size,
			widget.getMidiZero.size,
			widget.getSnapDistance.size,
			widget.getCtrlButtonGroup.size,
			widget.getMidiResolution.size
		], "(3) The number of midiConnectors should equal the size of the array returned by widget.getMidiMode");
		this.assertEquals(Set[widget.wmc.midiConnectors.m.value.size], Set[
			widget.wmc.midiDisplay.m.value.size,
			widget.wmc.midiOptions.m.value.size,
			widget.wmc.midiConnections.m.value.size,
			widget.wmc.midiConnectorNames.m.value.size
		], "(3) The number of midiConnectors should equal the size of the widget's midiDisplay, midiOptions, midiConnections, midiConnectorNames model value arrays");
		widget.removeMidiConnector(connection1);
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 2, "widget.wmc.midiConnectors.m.value should contain two midiConnectors after removing connection1");
		this.assertEquals(widget.wmc.midiConnectors.m.value.collect(_.name), ['MIDI Connection 1', \test], "widget.wmc.midiConnectors.m.value should contain two midiConnectors, named 'MIDI Connection 1' and 'test'");
		this.assertEquals(Set[widget.wmc.midiConnectors.m.value.size], Set[
			widget.getMidiMode.size,
			widget.getMidiZero.size,
			widget.getSnapDistance.size,
			widget.getCtrlButtonGroup.size,
			widget.getMidiResolution.size
		], "(4) The number of midiConnectors should equal the size of the array returned by widget.getMidiMode");
		this.assertEquals(Set[widget.wmc.midiConnectors.m.value.size], Set[
			widget.wmc.midiDisplay.m.value.size,
			widget.wmc.midiOptions.m.value.size,
			widget.wmc.midiConnections.m.value.size,
			widget.wmc.midiConnectorNames.m.value.size
		], "(4) The number of midiConnectors should equal the size of the widget's midiDisplay, midiOptions, midiConnections, midiConnectorNames model value  arrays");
	}

	test_set_getMidiMode {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getMidiMode, [0, 0, 0], "All widget.wmc.midiConnectors.m.value should be set to midiMode 0 by default");
		widget.setMidiMode(1);
		this.assertEquals(widget.getMidiMode, [1, 1, 1], "All widget.wmc.midiConnectors.m.value should have been set to midiMode 1");
		widget.setMidiMode(0, connection1);
		this.assertEquals(widget.getMidiMode, [1, 0, 1], "widget.wmc.midiConnectors.m.value midiMode should equal [1, 0, 1]");
		widget.setMidiMode(1, 1);
		this.assertEquals(widget.getMidiMode, [1, 1, 1], "widget.wmc.midiConnectors.m.value midiMode should equal [0, 1, 1]")
	}

	test_set_getMidiZero {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getMidiZero, [64, 64, 64], "All widget.wmc.midiConnectors.m.value should be set to midiZero 64 by default");
		widget.setMidiZero(0);
		this.assertEquals(widget.getMidiZero, [0, 0, 0], "All widget.wmc.midiConnectors.m.value should have been set to midiZero 0");
		widget.setMidiZero(64, connection1);
		this.assertEquals(widget.getMidiZero, [0, 64, 0], "widget.wmc.midiConnectors.m.value midiZero should equal [0, 64, 0]");
		widget.setMidiZero(64, 2);
		this.assertEquals(widget.getMidiZero, [0, 64, 64], "widget.wmc.midiConnectors.m.value midiZero should equal [0, 64, 64]");
	}

	test_set_getSnapDistance {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getSnapDistance, [0.1, 0.1, 0.1], "All widget.wmc.midiConnectors.m.value should be set to snapDistance 0.1 by default");
		widget.setSnapDistance(0.5);
		this.assertEquals(widget.getSnapDistance, [0.5, 0.5, 0.5], "All widget.wmc.midiConnectors.m.value should have been set to snapDistance 0.5");
		widget.setSnapDistance(0.1, connection1);
		this.assertEquals(widget.getSnapDistance, [0.5, 0.1, 0.5], "widget.wmc.midiConnectors.m.value snapDistance should equal [0.5, 0.1, 0.5]");
		widget.setSnapDistance(0.5, 0);
		this.assertEquals(widget.getSnapDistance, [0.5, 0.1, 0.5], "widget.wmc.midiConnectors.m.value snapDistance should equal [0.5, 0.1, 0.5]");
	}

	test_set_getCtrlButtonGroup {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getCtrlButtonGroup, [1, 1, 1], "All widget.wmc.midiConnectors.m.value should be set to ctrlButtonGroup 1 by default");
		widget.setCtrlButtonGroup(16);
		this.assertEquals(widget.getCtrlButtonGroup, [16, 16, 16], "All widget.wmc.midiConnectors.m.value should have been set to ctrlButtonGroup 16");
		widget.setCtrlButtonGroup(1, connection1);
		this.assertEquals(widget.getCtrlButtonGroup, [16, 1, 16], "widget.wmc.midiConnectors.m.value ctrlButtonGroup should equal [16, 1, 16]");
		widget.setCtrlButtonGroup(16, 1);
		this.assertEquals(widget.getCtrlButtonGroup, [16, 16, 16], "widget.wmc.midiConnectors.m.value ctrlButtonGroup should equal [16, 16, 16]");
	}

	test_set_getMidiResolution {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getMidiResolution, [1, 1, 1], "All widget.wmc.midiConnectors.m.value should be set to midiResolution 1 by default");
		widget.setMidiResolution(0.5);
		this.assertEquals(widget.getMidiResolution, [0.5, 0.5, 0.5], "All widget.wmc.midiConnectors.m.value should have been set to midiResolution 0.5");
		widget.setMidiResolution(1, connection1);
		this.assertEquals(widget.getMidiResolution, [0.5, 1, 0.5], "widget.wmc.midiConnectors.m.value midiResolution should equal [0.5, 1, 0.5]");
		widget.setMidiResolution(1, 2);
		this.assertEquals(widget.getMidiResolution, [0.5, 1, 1], "widget.wmc.midiConnectors.m.value midiResolution should equal [0.5, 1, 1]");
	}

	test_set_getMidiInputMapping {
		connection1 = widget.addMidiConnector;
		connection2 = widget.addMidiConnector;
		this.assertEquals(widget.getMidiInputMapping, [(mapping: \linlin), (mapping: \linlin), (mapping: \linlin)], "All widget.wmc.midiConnectors.m.value should have been set to (mapping: \linlin) by default.");
		widget.setMidiInputMapping(\lincurve, curve: 3);
		this.assertEquals(widget.getMidiInputMapping, [(mapping: \lincurve, curve: 3), (mapping: \lincurve, curve: 3), (mapping: \lincurve, curve: 3)], "All widget.wmc.midiConnectors.m.value should have been set to (mapping: \\lincurve, curve: 3).");
		widget.setMidiInputMapping(\linenv, env: Env([0, 0.2, 1], [0.5, 0.3], [-4, 4]), connector: connection2);
		this.assertEquals(widget.getMidiInputMapping, [(mapping: \lincurve, curve: 3), (mapping: \lincurve, curve: 3), (mapping: \linenv, env: Env([0, 0.2, 1], [0.5, 0.3], [-4, 4]))], "widget.wmc.midiConnectors.m.value at index 2 should have been set to (mapping: \\linenv, env: Env([0, 0.2, 1], [0.5, 0.3], [-4, 4]).");
		widget.setMidiInputMapping(\linexp, connector: 1);
		this.assertEquals(widget.getMidiInputMapping(connection1), (mapping: \linexp), "'connection1' (widget.wmc.midiConnectors.m.value at index 1) should have been set to (mapping: \\linexp.");
	}

	test_midiConnect {
		var numConnections;
		// MIDIIn.connectAll;
		numConnections = widget.wmc.midiConnectors.m.value.size;
		widget.midiConnect(0, num: 1);
		this.assert(numConnections == widget.wmc.midiConnectors.m.value.size, "The number of widget.wmc.midiConnectors.m.value should not have been increased after connecting the widget using the default midiConnection");
		this.assertEquals(widget.wmc.midiConnections.m[0].value, (num: 1), "After calling widget.midiConnect(0, num: 1) widget.wmc.midiConnection.m[0].value should equal (num: 1)");
		widget.midiConnect(num: 2);
		this.assert(widget.wmc.midiConnectors.m.value.size == (numConnections + 1) , "The number of widget.wmc.midiConnectors.m.value should not have been increased by 1 after connecting the widget without specifying a midiConnection");
		this.assertEquals(widget.wmc.midiConnections.m[1].value, (num: 2), "After calling widget.midiConnect(0, num: 1) widget.wmc.midiConnection.m[1].value should equal (num: 2)");
		// midi learn
		widget.midiConnect;
		MIDIIn.doControlAction(12345, 0, 5, 127);
		this.assertEquals(widget.wmc.midiConnections.m[2].value, (src: 12345, chan: 0, num: 5), "After calling widget.midiConnect widget.wmc.midiConnection.m[2].value should have set by 'learning' to' (src: 12345, chan: 0, num: 5)");
	}

	test_midiDisconnect {
		widget.midiConnect(0, num: 1);
		this.assertEquals(widget.wmc.midiConnections.m[0].value, (num: 1), "After calling widget.midiConnect(0, num: 1) widget.wmc.midi[0].midiConnection.m.value should equal (num: 1)");
		widget.midiDisconnect(0);
		this.assertEquals(widget.wmc.midiConnections.m[0].value, nil, "After calling widget.midiDisonnect(0) widget.wmc.midi[0].midiConnection.m.value should equal nil");
		widget.midiConnect(0, num: 1);
		this.assertEquals(widget.wmc.midiConnections.m[0].value, (num: 1), "After calling widgetDisConnect(0) and calling widget.midiConnect(0, num: 1) again widget.wmc.midi[0].midiConnection.m.value should equal (num: 1)");
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
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 1, activeActions: 1), "The widget should hold one action and one marked as active after calling addAction with arg 'active' set to true");
		this.assertEquals(widget.widgetActions[\active].key.class, SimpleController, "The widget.widgetActions should hold a SimpleController as key at key 'active'");
		widget.addAction(\inactive, { |cv, wdgt| wdgt.env.res2_(nil) }, false);
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 2, activeActions: 1), "The widget should hold two actions and one marked as active after calling addAction with arg 'active' set to false");
		this.assertEquals(widget.widgetActions[\inactive].key, nil, "The widget.widgetActions should hold a SimpleController as key at key 'inactive'");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res1], [0.5, \test], "The result of the evaluation of the custom action 'active' should be [0.5, 'test'] after setting the widgets cv's value");
		widget.addAction(\stringAction, "{ |cv, wdgt| wdgt.env.res3_([cv.value, wdgt.name]) }", true);
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 3, activeActions: 2), "The widget should hold three actions and two marked as active after calling addAction with arg 'active' set to true");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res3], [0.5, \test], "The result of the evaluation of the custom action 'stringAction' should be [0.5, 'test'] after setting the widgets cv's value");
	}

	test_activateAction {
		widget.addAction(\inactive, { |cv, wdgt| wdgt.env.res1_([cv.value, wdgt.name]) }, false);
		widget.activateAction(\inactive, true);
		this.assertEquals(widget.widgetActions[\inactive].key.class, SimpleController, "The widget.widgetActions should hold a SimpleController as key at key 'inactive' after calling activateAction");
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 1, activeActions: 1), "The widget should hold one action and one marked as active after calling activateAction with arg 'active' set to true");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env[\res1], [0.5, \test], "The result of the evaluation of the custom action 'inactive' should be [0.5, 'test'] after setting the widgets cv's value");
		widget.activateAction(\inactive, false);
		this.assertEquals(widget.widgetActions[\inactive].key, nil, "The widget.widgetActions should hold nil as key at key 'inactive' after calling activateAction with arg 'active' set to false");
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 1, activeActions: 0), "The widget should hold one action and one marked as inactive after calling activateAction with arg 'active' set to false");
	}

	test_removeAction {
		widget.addAction("active", {}, true);
		widget.addAction(\inactive, {}, false);
		widget.addAction(\stringAction, "{}", true);
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 3, activeActions: 2), "The widget should hold three actions and two of them should be marked as active");
		widget.removeAction(\active);
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 2, activeActions: 1), "The widget should hold two actions and one of them should be marked as active after removing the action 'active'");
		widget.removeAction(\inactive);
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 1, activeActions: 1), "The widget should hold one action and one should be marked as active after removing the action 'inactive'");
		widget.removeAction(\stringAction);
		this.assertEquals(widget.wmc.actions.m.value, (numActions: 0, activeActions: 0), "The widget should hold no actions after removing the action 'stringAction'");
	}

	test_updateAction {
		widget.addAction(\active, { |cv, wdgt| wdgt.env.res1_([cv.value, wdgt.name]) }, true);
		widget.cv.value_(0.5);
		this.assertEquals(widget.env.res1, [0.5, \test], "widget.env.res1 should equal [0.5, \test] after setting the widget cv's value");
		widget.updateAction(\active, { |cv, wdgt| wdgt.env.res1 = [cv.value, wdgt.getSpec] });
		widget.cv.value_(0);
		this.assertEquals(widget.env.res1, [0.0, ControlSpec(0, 1, 'linear', 0.0, 0.0, "")], "widget.env.res1 should equal [0.0, ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "")] after having updated the action and setting the widget cv's value to 0");
		widget.updateAction(\active, "{ |cv, wdgt| wdgt.env.res1_([cv.value, wdgt.name]) }");
		widget.cv.value_(0.5);
		this.assertEquals(widget.env.res1, [0.5, \test], "widget.env.res1 should equal [0.5, 'test'] after having updated the action and setting the widget cv's value");
	}
}