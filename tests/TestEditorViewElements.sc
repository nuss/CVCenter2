TestMidiConnectorSelect : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {
		var select = MidiConnectorSelect(widget: widget);
		this.assertEquals(MidiConnectorSelect.all[widget], List[select], "After creation of a new MidiConnectorSelect MidiConnectorSelect.all[widget] should hold a List with a single item which is the just created MidiConnectorSelect");
		this.assertEquals(select.view.items, ["Select connector...", 'MIDI Connection 1'], "After calling MidiConnectorSelect(widget: widget) the select's items should equal to [\"Select connector...\", 'MIDI Connection 1']");
		widget.addMidiConnector;
		this.assertEquals(select.view.items.size, 3, "After adding another MidiConnector the number of select items should have increased to 3.");
		widget.removeMidiConnector(0);
		this.assertEquals(select.view.items, ["Select connector...", 'MIDI Connection 2'], "After calling widget.removeMidiConnector(0) the select items should hold two items: [\"Select connector...\", 'MIDI Connection 2'].");
	}

	test_close {
		var select = MidiConnectorSelect(widget: widget);
		select.close;
		this.assertEquals(MidiConnectorSelect.all[widget], List[], "After calling select.close MidiConnectorSelect.all[widget] should contain an empty List");
	}
}

TestMidiLearnButton : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {
		var learnButton1 = MidiLearnButton(widget: widget), learnButton2;
		this.assertEquals(MidiLearnButton.all[widget], List[learnButton1], "After the creation of a new MidiLearnbutton MidiLearnButton.all[widget] should hold a List with a single item which is the just created MidiLearnButton.");
		this.assertEquals(widget.midiConnectors.indexOf(learnButton1.connector), 0, "After creation of the new MidiLearnButton its connector should be the one at index 0 in widget.midiConnectors.");
		this.assertEquals(widget.syncKeys.size, 2, "After creation of the new MidiLearnButton widget.syncKeys should return a list of two syncKeys.");
		learnButton2 = MidiLearnButton(widget: widget);
		this.assertEquals(MidiLearnButton.all[widget], List[learnButton1, learnButton2], "After creating another MidiLearnButton MidiLearnButton.all[widget] should hold a list of two MidiLearnButtons.");
		this.assertEquals(widget.syncKeys.size, 3, "After the creation of the new MidiLearnButton widget.syncKeys should return a list of three syncKeys.");
	}

	test_index {
		var learnButton = MidiLearnButton(widget: widget);
		var mc = widget.wmc.midiDisplay;
		widget.addMidiConnector;
		learnButton.index_(1);
		this.assertEquals(widget.midiConnectors.indexOf(learnButton.connector), 1, "After adding another MidiConnector to the widget and calling learnButton.index_(1) its connector should now be the one at position 1 in widget.midiConnectors.");
		learnButton.view.valueAction_(1);
		this.assertEquals(mc.model.collect(_.value), [(learn: "L", src: "source", chan: "chan", ctrl: "ctrl"), (learn: "X", src: "source", chan: "chan", ctrl: "ctrl")], "After calling valueAction on learnButton.view the widget's midiDisplay.model should hold Refs to two Events: [(learn: \"L\", src: \"source\", chan: \"chan\", ctrl: \"ctrl\"), (learn: \"X\", src: \"source\", chan: \"chan\", ctrl: \"ctrl\")] ");
		widget.removeMidiConnector(0);
		this.assertEquals(widget.midiConnectors.indexOf(learnButton.connector), 0, "After removing the connector at position 0 in widget.midiConnectors the remaining MidiConnector should be accessible at position 0 in widget.midiConnectors");
		this.assertEquals(mc.model.size, 1, "After removing the MidiConnector at position 0 in widget.midiConnectors the widget's midiDisplay.model should hold a single value");
	}
}

TestMidiSrcSelect : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {

	}
}

TestMidiChanField : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {

	}
}

TestMidiCtrlField : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {

	}
}

TestMidiModeSelect : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {

	}
}

TestMidiMeanNumberBox : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {

	}
}

TestSoftWithinNumberBox : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {

	}
}

TestMidiResolutionNumberBox : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {

	}
}

TestSlidersPerBankNumberTF : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {

	}
}