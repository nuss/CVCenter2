TestConnectorElementView : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiConnectorNameField(widget: widget);
		element2 = MidiConnectorNameField(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(MidiConnectorNameField.all[widget], List[element2], "After closing element1 the class' 'all' variable should hold a List with one element which is element2 under a key which is the widget itself.");
		this.assertEquals(widget.syncKeys, [\default, \midiConnectorName], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiConnectorName', after calling 'close'");
		element2.close;
		this.assertEquals(MidiConnectorNameField.all[widget], List[], "After closing element2 the class' 'all' variable should hold an empty List under a key which is the widget itself.");
		this.assertEquals(widget.syncKeys, [\default], "The widget's 'syncKeys' should contain a single Symbol, 'default' after calling 'close'");
	}
}

TestMidiConnectorNameField : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiConnectorNameField(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiConnectorNameField.all[widget][0] === element1, "MidiConnectorNameField's all variable at the key which is the widget itself should hold a List with one value: the element itself.");
		this.assertEquals(widget.syncKeys, [\default, \midiConnectorName], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiConnectorName', after creating a new MidiConnectorNameField");
		this.assert(element1.connector === widget.midiConnectors[0], "The elements connector should be identical with the connector at the widget's midiConnectors List");
		this.assertEquals(widget.wmc.midiConnectorNames.model.value, List['MIDI Connection 1'], "The widget's midiConnectorNames model should hold a List with one value 'MIDI Connection 1'");
		element1.view.valueAction_('new name');
		this.assertEquals(widget.wmc.midiConnectorNames.model.value, List['new name'], "After calling 'valueAction' on the element's view the widget's midiConnectorNames model should hold a List with one value 'new name'");
		this.assertEquals(widget.midiConnectors[0].name, 'new name', "widget.midiConnectors[0].name should return 'new name' after  calling element1.view.valueAction_('new name')");
		element2 = MidiConnectorNameField(widget: widget);
		this.assertEquals(MidiConnectorNameField.all[widget].size, 2, "MidiConnectorNameField.all[widget] should hold a List with 2 elements after creating another MidiConnectorNameField instance.");
		this.assertEquals(element2.view.string, "new name", "element2's textfield should hold a string 'new name'");
		element2.view.valueAction_('changed name');
		this.assertEquals(element1.view.string, "changed name", "After calling valueAction on element2's view with a value 'changed name' element1's view should have been updated accordingly");
		element2.connector.name_("ggggggg");
		this.assert(element1.string == "ggggggg" and:{ element2.string == "ggggggg" }, "After calling element2.connector.name_(\"ggggggg\") both, element1's and element2's string should have been set to \"ggggggg\".");
	}

	test_index_ {
		widget.addMidiConnector;
		element2 = MidiConnectorNameField(widget: widget, connectorID: 1);
		this.assertEquals(element2.connector, widget.midiConnectors[1], "On instantiation the new MidiConnectorNameField's connector should be widget.midiConnectors[1].");
		this.assertEquals(element2.view.string, "MIDI Connection 2", "After executing element2.index_(1) element2's view should hold a string 'MIDI Connection 2'");
		element1.index_(1);
		element2.view.valueAction_("another name");
		this.assertEquals(element1.view.string, "another name", "After calling element2.view.valueAction_(\"another name\") element1's view string should have been set to \"another name\"");
	}
}

TestMidiConnectorSelect : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiConnectorSelect(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiConnectorSelect.all[widget][0] === element1, "MidiConnectorSelect's all variable at the key which is the widget itself should hold a List with one value: the element itself.");
		this.assertEquals(widget.syncKeys, [\default, \midiConnectorSelect], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiConnectorName', after creating a new MidiConnectorNameField");
		this.assert(element1.connector === widget.midiConnectors[0], "The elements connector should be identical with the connector at the widget's midiConnectors List");
		this.assertEquals(widget.wmc.midiConnectorNames.model.value, List['MIDI Connection 1'], "The widget's midiConnectorNames model should hold a List with one value 'MIDI Connection 1'");
		this.assertEquals(element1.view.items, ['MIDI Connection 1', "add MidiConnector..."], "The MidiConnectorSelect's items should hold two items: ['MIDI Connection 1', \"add MidiConnector...\"]");
		element2 = MidiConnectorSelect(widget: widget);
		this.assertEquals(MidiConnectorSelect.all[widget].size, 2, "MidiConnectorSelect.all[widget] should hold a List with 2 elements after creating another MidiConnectorSelect instance.");
		element1.connector.name_("xyz");
		this.assertEquals(element1.item, "xyz", "After calling element1.connector.name_(\"xyz\") element1.item should return \"xyz\"");
		this.assertEquals(element2.item, "xyz", "After calling element1.connector.name_(\"xyz\") element2.item should return \"xyz\"");
		// can't test menu entries here as synchronisation of elements after changing select is
		// handled in MidiConnectorsEditorView:-init
	}

	test_index_ {
		widget.addMidiConnector;
		element2 = MidiConnectorSelect(widget: widget, connectorID: 1);
		this.assert(element2.connector === widget.midiConnectors[1], "After creating a new MidiConnectorSelect with connectorID set to 1 the MidiConnectorSelect's connector should be identical with widget.midiConnectors[1]");
		this.assertEquals(element2.view.value, 1, "element2.value should return 1.");
		element2.connector.name_("aaaaaa");
		this.assertEquals(element1.items[1], "aaaaaa", "After calling element2.connector.name_(\"aaaaaa\") element1.items[1] should return \"aaaaaa\".");
	}
}

TestMidiLearnButton : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiLearnButton(widget: widget);
		element2 = MidiLearnButton(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiLearnButton.all[widget].size == 2 and: {
			MidiLearnButton.all[widget][0] === element1
		}, "MidiLearnButton's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \midiLearnButton], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiLearnButton', after creating a new MidiLearnButton");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.view.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.value should return 1");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the MidiLearnButton's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1");
	}
}

TestMidiSrcSelect : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiSrcSelect(widget: widget);
		element2 = MidiSrcSelect(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiSrcSelect.all[widget].size == 2 and: {
			MidiSrcSelect.all[widget][0] === element1
		}, "MidiSrcSelect's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \midiSrcSelect], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiSrcSelect', after creating a new MidiSrcSelect");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.value should return 1");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the MidiSrcSelect's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1");
	}
}

TestMidiChanField : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiChanField(widget: widget);
		element2 = MidiChanField(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiChanField.all[widget].size == 2 and: {
			MidiChanField.all[widget][0] === element1
		}, "MidiChanField's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \midiChanField], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiChanField', after creating a new MidiChanField");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.string, "1", "After calling element1.valueAction_(1) element2.string should return \"1\"");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the MidiSrcSelect's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.string, "1", "After calling element2.valueAction_(1) and setting element1.index_(1) element1.string should return \"1\"");
	}
}

TestMidiCtrlField : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiCtrlField(widget: widget);
		element2 = MidiCtrlField(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiCtrlField.all[widget].size == 2 and: {
			MidiCtrlField.all[widget][0] === element1
		}, "MidiCtrlField's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \midiCtrlField], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiCtrlField', after creating a new MidiCtrlField");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.string, "1", "After calling element1.valueAction_(1) element2.string should return \"1\"");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the MidiCtrlField's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.string, "1", "After calling element2.valueAction_(1) and setting element1.index_(1) element1.string should return \"1\"");
	}
}

TestMidiModeSelect : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiModeSelect(widget: widget);
		element2 = MidiModeSelect(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiModeSelect.all[widget].size == 2 and: {
			MidiModeSelect.all[widget][0] === element1
		}, "MidiModeSelect's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \midiModeSelect], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiModeSelect', after creating a new MidiModeSelect");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.value should return 1");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the MidiSrcSelect's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1");
	}
}

TestMidiZeroNumberBox : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiZeroNumberBox(widget: widget);
		element2 = MidiZeroNumberBox(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiZeroNumberBox.all[widget].size == 2 and: {
			MidiZeroNumberBox.all[widget][0] === element1
		}, "MidiZeroNumberBox's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \midiZeroNumberBox], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiZeroNumberBox', after creating a new MidiZeroNumberBox");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.string should return 1");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the MidiZeroNumberBox's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1.");
	}
}

TestSnapDistanceNumberBox : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = SnapDistanceNumberBox(widget: widget);
		element2 = SnapDistanceNumberBox(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(SnapDistanceNumberBox.all[widget].size == 2 and: {
			SnapDistanceNumberBox.all[widget][0] === element1
		}, "SnapDistanceNumberBox's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \snapDistanceNumberBox], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'snapDistanceNumberBox', after creating a new SnapDistanceNumberBox");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.string should return 1");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the SnapDistanceNumberBox's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1.");
	}
}

TestMidiResolutionNumberBox : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiResolutionNumberBox(widget: widget);
		element2 = MidiResolutionNumberBox(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiResolutionNumberBox.all[widget].size == 2 and: {
			MidiResolutionNumberBox.all[widget][0] === element1
		}, "MidiResolutionNumberBox's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \midiResolutionNumberBox], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiResolutionNumberBox', after creating a new MidiResolutionNumberBox");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.string should return 1");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the MidiResolutionNumberBox's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1.");
	}
}

TestSlidersPerGroupNumberTF : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = SlidersPerGroupNumberTF(widget: widget);
		element2 = SlidersPerGroupNumberTF(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(SlidersPerGroupNumberTF.all[widget].size == 2 and: {
			SlidersPerGroupNumberTF.all[widget][0] === element1
		}, "SlidersPerGroupNumberTF's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget.syncKeys, [\default, \slidersPerGroupNumberTF], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'slidersPerGroupNumberTF', after creating a new SlidersPerGroupNumberTF");
		this.assert(element1.connector === widget.midiConnectors[0] and: {
			element2.connector === widget.midiConnectors[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.string, "1", "After calling element1.valueAction_(1) element2.string should return \"1\"");
	}

	test_index_ {
		widget.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget.midiConnectors[1], "After setting element2.index_(1) the SlidersPerGroupNumberTF's connector should be identical with widget.midiConnectors[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.string, "1", "After calling element2.valueAction_(1) and setting element1.index_(1) element1.string should return \"1\"");
	}
}

TestMidiInitButton : UnitTest {
	var button1, button2;

	setup {
		button1 = MidiInitButton.new;
		button2 = MidiInitButton.new;
	}

	tearDown {
		button1.remove;
		button2.remove;
	}

	test_new {
		// this.tearDown;
		this.assertEquals(CVWidget.syncKeys, [\default], "CVWidget.syncKeys should hold a single value 'default' before creating a MidiInitButton instance.");
		button1 = MidiInitButton.new;
		this.assertEquals(MidiInitButton.all.size, 1, "After creating a MidiInitButton instance MidiInitButton.all should have a size of 1");
		this.assertEquals(CVWidget.syncKeys, [\default, \midiInitButton], "CVWidget.syncKeys should hold two values after creating a MidiInitButton instance: 'default'  and 'midiInitButton'");
		button2 = MidiInitButton.new;
		this.assertEquals(MidiInitButton.all.size, 2, "After creating a second MidiInitButton instance MidiInitButton.all should have grown to a size of 2");
		this.ifAsserts(MIDIClient.initialized.not, "MIDIClient not initialized", {
			this.assert(button1.view.states == [["init MIDI", Color(), Color(0.0, 1.0)]] && button2.view.states == [["init MIDI", Color(), Color(0.0, 1.0)]], "MIDIClient not initialized")
		});
		button1.doAction;
		this.ifAsserts(MIDIClient.initialized, "MIDIClient initialized", {
			this.assert(button1.view.states == [["restart MIDI", Color(1.0, 1.0, 1.0), Color(1.0)]] && button2.view.states == [["restart MIDI", Color(1.0, 1.0, 1.0), Color(1.0)]], "MIDIClient initialized")
		})
	}

	test_click {

	}

}