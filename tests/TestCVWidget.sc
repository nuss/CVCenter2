TestAbstractCVWidget : UnitTest {

	test_globalSetup {
		var setup = AbstractCVWidget.globalSetup;
		this.assertEquals(setup.midiMode, 0, "AbstractCVWidget.midiMode should be 0 by default");
		this.assertEquals(setup.midiResolution, 1, "AbstractCVWidget.midiResolution should be 1 by default");
		this.assertEquals(setup.midiMean, 0.1, "AbstractCVWidget.midiMean should be 0.1 by default");
		this.assertEquals(setup.ctrlButtonBank, nil, "AbstractCVWidget.ctrlButtonBank should be nil by default");
		this.assertEquals(setup.softWithin, 0.1, "AbstractCVWidget.softWithin should be 0.1 by default");
	}

}


TestCVWidgetKnob : UnitTest {

	var widget, midiConnection, oscConnection;

	setUp {
		widget = CVWidgetKnob(\knob);
	}

	test_new {
		var oscConnection, midiConnection;
		this.assertEquals(widget.class, CVWidgetKnob, "A new CVWidgetKnob should identify itself as a CVWidgetKnob");
		this.assertEquals(widget.cv.class, CV, "A new CVWidgetKnob for which no CV has been specified should automatically have been created with a new CV");
		this.assertEquals(widget.cv.spec, \unipolar.asSpec, "A new CVWidgetKnob's CV should equal \unipolar.asSpec");
		this.assertEquals(widget.syncKeys, [\default], "A new CVWidgetKnob should initialize the syncKeys array with a single key \default");
		this.assertEquals(widget.wmc.class, Event, "A new CVWidgetKnob should initialize an Event kept in a variable named wmc");
		this.assertEquals(widget.wmc.keys, Set[\cvSpec, \actions, \osc, \midi], "A CVWidgetKnob's wmc variable (an Event) should by default hold 4 keys: \cvSpec, \actions, \osc, \midi");
		oscConnection = OscConnection(widget);
		this.assertEquals(widget.oscConnections.size, 1, "After adding an empty OscConnection to the widget the widget's 'oscConnections' should hold one OscConnection");
		this.assertEquals(widget.oscConnections.keys.includes('OSC Connection 1'), true, "A new anonymous OscConnection should be named 'OSC Connection', appended by a number")
		midiConnection = MidiConnection(widget);
		this.assertEquals(widget.midiConnections.size, 1, "After adding an empty MidiConnection to the widget the widget's 'midiConnections' should hold one MidiConnection");
		this.assertEquals(widget.midiConnections.keys.includes('MIDI Connection 1'), true, "A new anonymous MidiConnection should be named 'MIDI Connection', appended by a number")
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

	test_setMidiMode {

	}

	test_getMidiMode {}

	test_setMidiMean {}

	test_getMidiMean {}

	test_setSoftWithin {}

	test_getSoftWithin {}

	test_setCtrlButtonBank {}

	test_getCtrlButtonBank {}

	test_setMidiResolution {}

	test_getMidiResolution {}

	test_midiConnect {}

	test_midiDisconnect {}

	test_setOscCalibration {}

	test_getOscCalibration {}

	test_setOscMapping {}

	test_getOscMapping {}

	test_setOscInputConstraints {}

	test_getOscInputConstraints {}

	test_oscConnect {}

	test_oscDisconnect {}

}