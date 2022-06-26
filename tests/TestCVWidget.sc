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

	test_new {
		var widget = CVWidgetKnob(\knob);
		this.assertEquals(widget.class, CVWidgetKnob, "A new CVWidgetKnob should identify itself as a CVWidgetKnob");
		this.assertEquals(widget.cv.class, CV, "A new CVWidgetKnob for which no CV has been specified should automatically have been created with a new CV");
		this.assertEquals(widget.cv.spec, \unipolar.asSpec, "A new CVWidgetKnob's CV should equal \unipolar.asSpec");
		this.assertEquals(widget.syncKeys, [\default], "A new CVWidgetKnob should initialize the syncKeys array with a single key \default");
		this.assertEquals(widget.wmc.class, Event, "A new CVWidgetKnob should initialize an Event kept in a variable named wmc");
		this.assertEquals(widget.wmc.keys, Set[\cvSpec, \actions, \osc, \midi], "A CVWidgetKnob's wmc variable (an Event) should by default hold 4 keys: \cvSpec, \actions, \osc, \midi");
		// TODO: check controllers
	}

	test_setSpec {}

	test_getSpec {}

	test_setMidiMode {}

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