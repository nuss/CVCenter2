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
		var learnButton1 = MidiLearnButton(widget: widget), learnButton2, learnButton3;
		var mc = widget.wmc.midiDisplay;
		var aConnector, syncKeyMatches;

		this.assertEquals(MidiLearnButton.all[widget], List[learnButton1], "After the creation of a new MidiLearnbutton MidiLearnButton.all[widget] should hold a List with a single item which is the just created MidiLearnButton.");
		this.assertEquals(widget.midiConnectors.indexOf(learnButton1.connector), 0, "After creation of the new MidiLearnButton its connector should be the one at index 0 in widget.midiConnectors.");
		this.assertEquals(widget.syncKeys.size, 2, "After creation of the new MidiLearnButton widget.syncKeys should return a list of two syncKeys.");
		syncKeyMatches = widget.syncKeys.select { |key| "^midiLearnButton_[0-9]+$".matchRegexp(key.asString) };
		this.assertEquals(syncKeyMatches.size, 1, "widget.syncKeys should return one key that matches the pattern \"^midiLearnButton_[0-9]+$\"");
		aConnector = widget.addMidiConnector;
		mc.model[widget.midiConnectors.indexOf(aConnector)].value_((learn: "X", src: "source...", chan: "chan", ctrl: "ctrl"));
		learnButton2 = MidiLearnButton(widget: widget, connectorID: 1);
		this.assertEquals(learnButton2.view.value, widget.midiConnectors.indexOf(aConnector), "Setting a connector's midiDisplay model should have been picked up by a new MidiLearnButton.");
		learnButton3 = MidiLearnButton(widget: widget);
		this.assertEquals(MidiLearnButton.all[widget], List[learnButton1, learnButton2, learnButton3], "After creating another MidiLearnButton MidiLearnButton.all[widget] should hold a list of two MidiLearnButtons.");
		this.assertEquals(widget.midiConnectors.indexOf(learnButton3.connector), 0, "After creation of another MidiLearnButton its connector should be the one at index 0 in widget.midiConnectors.");
		syncKeyMatches = widget.syncKeys.select { |key| "^midiLearnButton_[0-9]+$".matchRegexp(key.asString) };
		this.assertEquals(widget.syncKeys.size, 4, "After adding another MidiLearnButton widget.syncKeys should hold 3 keys.");
		this.assertEquals(syncKeyMatches.size, 3, "widget.syncKeys should hold two keys that match the pattern \"^midiLearnButton_[0-9]+$\"");
	}

	test_index_ {
		var learnButton = MidiLearnButton(widget: widget);
		var mc = widget.wmc.midiDisplay;

		widget.addMidiConnector;
		learnButton.index_(1);
		this.assertEquals(widget.midiConnectors.indexOf(learnButton.connector), 1, "After adding another MidiConnector to the widget and calling learnButton.index_(1) its connector should now be the one at position 1 in widget.midiConnectors.");
		learnButton.view.valueAction_(1);
		this.assertEquals(mc.model.collect(_.value), [(learn: "L", src: "source...", chan: "chan", ctrl: "ctrl"), (learn: "X", src: "source...", chan: "chan", ctrl: "ctrl")], "After calling valueAction on learnButton.view the widget's midiDisplay.model should hold Refs to two Events: [(learn: \"L\", src: \"source\", chan: \"chan\", ctrl: \"ctrl\"), (learn: \"X\", src: \"source\", chan: \"chan\", ctrl: \"ctrl\")] ");
		widget.removeMidiConnector(0);
		this.assertEquals(widget.midiConnectors.indexOf(learnButton.connector), 0, "After removing the connector at position 0 in widget.midiConnectors the remaining MidiConnector should be accessible at position 0 in widget.midiConnectors");
		this.assertEquals(mc.model.size, 1, "After removing the MidiConnector at position 0 in widget.midiConnectors the widget's midiDisplay.model should hold a single value");
	}

	test_close {
		var learnButton = MidiLearnButton(widget: widget);
		var syncKeyMatches;
		var mc = widget.wmc.midiDisplay;

		learnButton.close;
		this.assertEquals(MidiLearnButton.all[widget], List[], "After calling 'close' on a MidiLearnButton the instance should have been removed from the List at MidiLearnButton.all[widget].");
		syncKeyMatches = widget.syncKeys.select { |key| "^midiLearnButton_[0-9]+$".matchRegexp(key.asString) };
		this.assertEquals(syncKeyMatches.size, 0, "After closing the MidiLearnButton its syncKey should have been removed from widget.syncKeys.");
	}
}

TestMidiSrcSelect : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
		MidiSrcSelect.midiSources_([12345, 15243]);
	}

	tearDown {
		widget.remove;
	}

	test_new {
		var midiSrcSel1 = MidiSrcSelect(widget: widget), midiSrcSel2, midiSrcSel3;
		var mc = widget.wmc.midiDisplay;
		var aConnector, syncKeyMatches;


		this.assertEquals(MidiSrcSelect.all[widget], List[midiSrcSel1], "After the creation of a new MidiSrcSelect MidiSrcSelect.all[widget] should hold a List with a single item which is the just created MidiSrcSelect.");
		this.assertEquals(widget.midiConnectors.indexOf(midiSrcSel1.connector), 0, "After creation of the new MidiSrcSelect its connector should be the one at index 0 in widget.midiConnectors.");
		this.assertEquals(widget.syncKeys.size, 2, "After creation of the new MidiSrcSelect widget.syncKeys should return a list of two syncKeys.");
		syncKeyMatches = widget.syncKeys.select { |key| "^midiSrcSelect_[0-9]+$".matchRegexp(key.asString) };
		this.assertEquals(syncKeyMatches.size, 1, "widget.syncKeys should return one key that matches the pattern \"^midiSrcSelect_[0-9]+$\"");
		aConnector = widget.addMidiConnector;
		mc.model[widget.midiConnectors.indexOf(aConnector)].value_((learn: "L", src: 12345, chan: "chan", ctrl: "ctrl"));
		midiSrcSel2 = MidiSrcSelect(widget: widget, connectorID: 1);
		this.debug("midiSrcSel2.view.item.class: %".format(midiSrcSel2.view.item.class));
		this.assertEquals(midiSrcSel2.view.item, 12345, "Setting a connector's midiDisplay model should have been picked up by a new MidiSrcSelect.");
		midiSrcSel3 = MidiSrcSelect(widget: widget);
		this.assertEquals(MidiSrcSelect.all[widget], List[midiSrcSel1, midiSrcSel2, midiSrcSel3], "After creating another MidiSrcSelect MidiSrcSelect.all[widget] should hold a list of two MidiSrcSelects.");
		this.assertEquals(widget.midiConnectors.indexOf(midiSrcSel3.connector), 0, "After creation of another MidiSrcSelect its connector should be the one at index 0 in widget.midiConnectors.");
		syncKeyMatches = widget.syncKeys.select { |key| "^midiSrcSelect_[0-9]+$".matchRegexp(key.asString) };
		this.assertEquals(widget.syncKeys.size, 4, "After adding another MidiSrcSelect widget.syncKeys should hold 3 keys.");
		this.assertEquals(syncKeyMatches.size, 3, "widget.syncKeys should hold two keys that match the pattern \"^midiSrcSelect_[0-9]+$\"");
	}

	test_index_ {
		var midiSrcSelect = MidiSrcSelect(widget: widget);
		var mc = widget.wmc.midiDisplay;

		widget.addMidiConnector;
		midiSrcSelect.index_(1);
		this.assertEquals(widget.midiConnectors.indexOf(midiSrcSelect.connector), 1, "After adding another MidiConnector to the widget and calling midiSrcSelect.index_(1) its connector should now be the one at position 1 in widget.midiConnectors.");
		this.debug("midiSrcSelect.view.items: %".format(midiSrcSelect.view.items));
		// NOTE: might change...
		midiSrcSelect.view.valueAction_(1);
		this.assertEquals(mc.model.collect(_.value), [(learn: "L", src: "source...", chan: "chan", ctrl: "ctrl"), (learn: "L", src: 12345, chan: "chan", ctrl: "ctrl")], "After calling valueAction on midiSrcSelect.view the widget's midiDisplay.model should hold Refs to two Events: [(learn: \"L\", src: \"source...\", chan: \"chan\", ctrl: \"ctrl\"), (learn: \"L\", src: 12345, chan: \"chan\", ctrl: \"ctrl\")] ");
		widget.removeMidiConnector(0);
		this.assertEquals(widget.midiConnectors.indexOf(midiSrcSelect.connector), 0, "After removing the connector at position 0 in widget.midiConnectors the remaining MidiConnector should be accessible at position 0 in widget.midiConnectors");
		this.assertEquals(mc.model.size, 1, "After removing the MidiConnector at position 0 in widget.midiConnectors the widget's midiDisplay.model should hold a single value");
	}

	test_close {

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

	test_index_ {

	}

	test_close {

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

	test_index_ {

	}

	test_close {

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

	test_index_ {

	}

	test_close {

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

	test_index_ {

	}

	test_close {

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

	test_index_ {

	}

	test_close {

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

	test_index_ {

	}

	test_close {

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

	test_index_ {

	}

	test_close {

	}
}