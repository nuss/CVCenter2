TestConnectorElementView : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiConnectorNameField(widget: widget1);
		element2 = MidiConnectorNameField(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}
}

TestMidiConnectorNameField : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiConnectorNameField(widget: widget1);
	}

	tearDown {
		element1.close;
		widget1.remove;
	}

	test_new {
		this.assert(MidiConnectorNameField.all[widget1][0] === element1, "MidiConnectorNameField's all variable at the key which is the widget itself should hold a List with one value: the element itself.");
		this.assertEquals(widget1.syncKeys, [\default, MidiConnectorNameField.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'MidiConnectorNameField', after creating a new MidiConnectorNameField");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0], "The elements connector should be identical with the connector at the widget's midiConnectors List at index 0");
		element1.view.valueAction_('new name');
		this.assertEquals(widget1.wmc.midiConnectorNames.m.value, List['new name'], "After calling 'valueAction' on the element's view the widget's midiConnectorNames model should hold a List with one value 'new name'");
		this.assertEquals(widget1.wmc.midiConnectors.m.value[0].name, 'new name', "widget1.wmc.midiConnectors.m.value[0].name should return 'new name' after  calling element1.view.valueAction_('new name')");
		element2 = MidiConnectorNameField(widget: widget1);
		this.assertEquals(MidiConnectorNameField.all[widget1].size, 2, "MidiConnectorNameField.all[widget1] should hold a List with 2 elements after creating another MidiConnectorNameField instance.");
		this.assertEquals(element2.view.string, "new name", "element2's textfield should hold a string 'new name'");
		element2.view.valueAction_('changed name');
		this.assertEquals(element1.view.string, "changed name", "After calling valueAction on element2's view with a value 'changed name' element1's view should have been updated accordingly");
		element2.connector.name_("ggggggg");
		this.assert(element1.string == "ggggggg" and:{ element2.string == "ggggggg" }, "After calling element2.connector.name_(\"ggggggg\") both, element1's and element2's string should have been set to \"ggggggg\".");
		element2.close;
	}

	test_index_ {
		widget1.addMidiConnector;
		element2 = MidiConnectorNameField(widget: widget1, connectorID: 1);
		this.assertEquals(element2.connector, widget1.wmc.midiConnectors.m.value[1], "On instantiation the new MidiConnectorNameField's connector should be widget1.wmc.midiConnectors.m.value[1].");
		this.assertEquals(element2.view.string, "MIDI Connection 2", "After executing element2.index_(1) element2's view should hold a string 'MIDI Connection 2'");
		element1.index_(1);
		element2.view.valueAction_("another name");
		this.assertEquals(element1.view.string, "another name", "After calling element2.view.valueAction_(\"another name\") element1's view string should have been set to \"another name\"");
		element2.close;
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		widget2.wmc.midiConnectors.m.value[0].name_(\qqqqqqq);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiConnectorNameField with arg 'widget' set to widget2 the MidiConnectorNameField's 'widget' getter should returm widget2");
		this.assertEquals(element1.string.asSymbol, widget2.wmc.midiConnectorNames.m.value[0], "The MidiConnectorNameField's TextField should have been set to the name of the currently set value in widget2.wmc.midiConnectorNames.m.value[0]");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element1 widget1.syncKeys should hold one remaining value: 'default'.");
	}
}

TestMidiConnectorSelect : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiConnectorSelect(widget: widget1);
	}

	tearDown {
		element1.close;
		widget1.remove;
	}

	test_new {
		this.assert(MidiConnectorSelect.all[widget1][0] === element1, "MidiConnectorSelect's all variable at the key which is the widget itself should hold a List with one value: the element itself.");
		this.assertEquals(widget1.syncKeys, [\default, MidiConnectorSelect.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'MidiConnectorSelect', after creating a new MidiConnectorSelect");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0], "The elements connector should be identical with the connector at the widget's midiConnectors List");
		this.assertEquals(widget1.wmc.midiConnectorNames.m.value, List['MIDI Connection 1'], "The widget's midiConnectorNames model should hold a List with one value 'MIDI Connection 1'");
		this.assertEquals(element1.view.items, ['MIDI Connection 1', 'add MidiConnector...'], "The MidiConnectorSelect's items should hold two items: ['MIDI Connection 1', 'add MidiConnector...']");
		element2 = MidiConnectorSelect(widget: widget1);
		this.assertEquals(MidiConnectorSelect.all[widget1].size, 2, "MidiConnectorSelect.all[widget1] should hold a List with 2 elements after creating another MidiConnectorSelect instance.");
		element1.connector.name_("xyz");
		this.assertEquals(element1.item, \xyz, "After calling element1.connector.name_(\"xyz\") element1.item should return 'xyz'");
		this.assertEquals(element2.item, \xyz, "After calling element1.connector.name_(\"xyz\") element2.item should return 'xyz'");
		// can't test menu entries here as synchronisation of elements after changing select is
		// handled in MidiConnectorsEditorView:-init
		element2.close;
	}

	test_index_ {
		widget1.addMidiConnector;
		element2 = MidiConnectorSelect(widget: widget1, connectorID: 1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After creating a new MidiConnectorSelect with connectorID set to 1 the MidiConnectorSelect's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		this.assertEquals(element2.view.value, 1, "element2.value should return 1.");
		element2.connector.name_("aaaaaa");
		this.assertEquals(element1.items[1], \aaaaaa, "After calling element2.connector.name_(\"aaaaaa\") element1.items[1] should return 'aaaaaa'.");
		element2.close;
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		widget2.wmc.midiConnectors.m.value[0].name(\qqqqqq);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiConnectorSelect with arg 'widget' set to widget2 the MidiConnectorSelect's 'widget' getter should return widget2");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element1 widget1.syncKeys should hold one remaining value: 'default'.");
	}
}

TestMidiLearnButton : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiLearnButton(widget: widget1);
		element2 = MidiLearnButton(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}

	test_new {
		this.assert(MidiLearnButton.all[widget1].size == 2 and: {
			MidiLearnButton.all[widget1][0] === element1
		}, "MidiLearnButton's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, MidiLearnButton.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'MidiLearnButton', after creating a new MidiLearnButton");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.view.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.value should return 1");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the MidiLearnButton's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(1);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiLearnButton with arg 'widget' set to widget2 the MidiLearnButton's 'widget' getter should return widget2");
		this.assertEquals(element1.view.value, 0, "After setting element1.valueAction_(1) should leave element1's value at 0 after calling element1.widget_(widget2).");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default, element1.class.asSymbol], "After closing element1 widget1.syncKeys should hold two value: 'default' and '%'".format(element1.class.asSymbol));
		element2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element2 widget1.syncKeys should hold one remaining value: 'default'");
	}
}

TestMidiSrcSelect : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiSrcSelect(widget: widget1);
		element2 = MidiSrcSelect(widget: widget1);
		CVWidget.wmc.midiSources.m.value_((abcd: 123, efgh: 456)).changedPerformKeys(CVWidget.syncKeys);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
		CVWidget.wmc.midiSources.m.value_(()).changedPerformKeys(CVWidget.syncKeys);
	}

	test_new {
		this.assert(MidiSrcSelect.all[widget1].size == 2 and: {
			MidiSrcSelect.all[widget1][0] === element1
		}, "MidiSrcSelect's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, MidiSrcSelect.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'MidiSrcSelect', after creating a new MidiSrcSelect");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.view.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.value should return 1");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the MidiSrcSelect's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(1);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiSrcSelect with arg 'widget' set to widget2 the MidiSrcSelect's 'widget' getter should return widget2");
		this.assertEquals(element1.view.value, 0, "After setting element1.valueAction_(1) should leave element1's value at 0 after calling element1.widget_(widget2).");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default, element1.class.asSymbol], "After closing element1 widget1.syncKeys should hold two value: 'default' and '%'".format(element1.class.asSymbol));
		this.assertEquals(CVWidget.syncKeys, [\default, element1.class.asSymbol], "After closing button1 CVWidget.syncKeys should hold two values: 'default' and '%'.".format(element1.class.asSymbol));
		element2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element2 widget1.syncKeys should hold one remaining value: 'default'");
		this.assertEquals(CVWidget.syncKeys, [\default], "After closing element1 CVWidget.syncKeys should hold  one remaining value: 'default'");
	}
}

TestMidiChanField : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiChanField(widget: widget1);
		element2 = MidiChanField(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}

	test_new {
		this.assert(MidiChanField.all[widget1].size == 2 and: {
			MidiChanField.all[widget1][0] === element1
		}, "MidiChanField's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, MidiChanField.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'MidiChanField', after creating a new MidiChanField");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.string, "1", "After calling element1.valueAction_(1) element2.string should return \"1\"");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the MidiSrcSelect's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.string, "1", "After calling element2.valueAction_(1) and setting element1.index_(1) element1.string should return \"1\"");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(1);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiChanField with arg 'widget' set to widget2 the MidiChanField's 'widget' getter should return widget2");
		this.assertEquals(element1.string, "chan", "After setting element1.valueAction_(1) should leave element1's string at \"chan\" after calling element1.widget_(widget2).");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default, element1.class.asSymbol], "After closing element1 widget1.syncKeys should hold two value: 'default' and '%'".format(element1.class.asSymbol));
		element2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element2 widget1.syncKeys should hold one remaining value: 'default'");
	}
}

TestMidiCtrlField : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiCtrlField(widget: widget1);
		element2 = MidiCtrlField(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}

	test_new {
		this.assert(MidiCtrlField.all[widget1].size == 2 and: {
			MidiCtrlField.all[widget1][0] === element1
		}, "MidiCtrlField's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, MidiCtrlField.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'MidiCtrlField', after creating a new MidiCtrlField");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.string, "1", "After calling element1.valueAction_(1) element2.string should return \"1\"");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the MidiCtrlField's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.string, "1", "After calling element2.valueAction_(1) and setting element1.index_(1) element1.string should return \"1\"");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(1);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiCtrlField with arg 'widget' set to widget2 the MidiCtrlField's 'widget' getter should return widget2");
		this.assertEquals(element1.string, "ctrl", "After setting element1.valueAction_(1) should leave element1's string at \"ctrl\" after calling element1.widget_(widget2).");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default, element1.class.asSymbol], "After closing element1 widget1.syncKeys should hold two value: 'default' and '%'".format(element1.class.asSymbol));
		element2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element2 widget1.syncKeys should hold one remaining value: 'default'");
	}
}

TestMidiModeSelect : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiModeSelect(widget: widget1);
		element2 = MidiModeSelect(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}

	test_new {
		this.assert(MidiModeSelect.all[widget1].size == 2 and: {
			MidiModeSelect.all[widget1][0] === element1
		}, "MidiModeSelect's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, MidiModeSelect.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'MidiModeSelect', after creating a new MidiModeSelect");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.value should return 1");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the MidiSrcSelect's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(1);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiModeSelect with arg 'widget' set to widget2 the MidiModeSelect's 'widget' getter should return widget2");
		this.assertEquals(element1.view.value, 0, "After setting element1.valueAction_(1) should leave element1's value at 0 after calling element1.widget_(widget2).");
		widget2.remove;
	}
}

TestMidiZeroNumberBox : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiZeroNumberBox(widget: widget1);
		element2 = MidiZeroNumberBox(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}

	test_new {
		this.assert(MidiZeroNumberBox.all[widget1].size == 2 and: {
			MidiZeroNumberBox.all[widget1][0] === element1
		}, "MidiZeroNumberBox's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, MidiZeroNumberBox.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'MidiZeroNumberBox', after creating a new MidiZeroNumberBox");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.string should return 1");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the MidiZeroNumberBox's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1.");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(60);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiZeroNumberBox with arg 'widget' set to widget2 the MidiZeroNumberBox's 'widget' getter should return widget2");
		this.assertEquals(element1.view.value, 64, "After setting element1.valueAction_(60) should leave element1's value at 64 after calling element1.widget_(widget2).");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default, element1.class.asSymbol], "After closing element1 widget1.syncKeys should hold two value: 'default' and '%'".format(element1.class.asSymbol));
		element2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element2 widget1.syncKeys should hold one remaining value: 'default'");
	}
}

TestSnapDistanceNumberBox : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = SnapDistanceNumberBox(widget: widget1);
		element2 = SnapDistanceNumberBox(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}

	test_new {
		this.assert(SnapDistanceNumberBox.all[widget1].size == 2 and: {
			SnapDistanceNumberBox.all[widget1][0] === element1
		}, "SnapDistanceNumberBox's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, SnapDistanceNumberBox.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'SnapDistanceNumberBox', after creating a new SnapDistanceNumberBox");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.string should return 1");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the SnapDistanceNumberBox's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1.");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(1);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the SnapDistanceNumberBox with arg 'widget' set to widget2 the SnapDistanceNumberBox's 'widget' getter should return widget2");
		this.assertEquals(element1.view.value, 0.1, "After setting element1.valueAction_(1) should leave element1's value at 0.1 after calling element1.widget_(widget2).");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default, element1.class.asSymbol], "After closing element1 widget1.syncKeys should hold two value: 'default' and '%'".format(element1.class.asSymbol));
		element2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element2 widget1.syncKeys should hold one remaining value: 'default'");
	}
}

TestMidiResolutionNumberBox : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = MidiResolutionNumberBox(widget: widget1);
		element2 = MidiResolutionNumberBox(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}

	test_new {
		this.assert(MidiResolutionNumberBox.all[widget1].size == 2 and: {
			MidiResolutionNumberBox.all[widget1][0] === element1
		}, "MidiResolutionNumberBox's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, MidiResolutionNumberBox.asSymbol], "The widget's 'syncKeys' should contain two Symbols, 'default' and 'MidiResolutionNumberBox', after creating a new MidiResolutionNumberBox");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The element's connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(1);
		this.assertEquals(element2.view.value, 1, "After calling element1.valueAction_(1) element2.value should return 1");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the MidiResolutionNumberBox's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(1);
		element1.index_(1);
		this.assertEquals(element1.view.value, 1, "After calling element2.valueAction_(1) and setting element1.index_(1) element1.view.value should return 1.");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(0.5);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the MidiResolutionNumberBox with arg 'widget' set to widget2 the MidiResolutionNumberBox's 'widget' getter should return widget2");
		this.assertEquals(element1.view.value, 1, "After setting element1.valueAction_(0.5) should leave element1's value at 1 after calling element1.widget_(widget2).");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default, element1.class.asSymbol], "After closing element1 widget1.syncKeys should hold two value: 'default' and '%'".format(element1.class.asSymbol));
		element2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element2 widget1.syncKeys should hold one remaining value: 'default'");
	}
}

TestSlidersPerGroupNumberBox : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = SlidersPerGroupNumberBox(widget: widget1);
		element2 = SlidersPerGroupNumberBox(widget: widget1);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}

	test_new {
		this.assert(SlidersPerGroupNumberBox.all[widget1].size == 2 and: {
			SlidersPerGroupNumberBox.all[widget1][0] === element1
		}, "SlidersPerGroupNumberBox's all variable at the key which is the widget itself should hold a List with two elements of which element1 is held at index 0");
		this.assertEquals(widget1.syncKeys, [\default, SlidersPerGroupNumberBox.asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'SlidersPerGroupNumberBox', after creating a new SlidersPerGroupNumberBox");
		this.assert(element1.connector === widget1.wmc.midiConnectors.m.value[0] and: {
			element2.connector === widget1.wmc.midiConnectors.m.value[0]
		}, "The elements connector should be identical with the first connector in the widget's midiConnectors List");
		element1.valueAction_(4);
		this.assertEquals(element2.view.value, 4, "After calling element1.valueAction_(4) element2.string should return 4");
	}

	test_index_ {
		widget1.addMidiConnector;
		element2.index_(1);
		this.assert(element2.connector === widget1.wmc.midiConnectors.m.value[1], "After setting element2.index_(1) the SlidersPerGroupNumberBox's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		element2.valueAction_(3);
		element1.index_(1);
		this.assertEquals(element1.view.value, 3, "After calling element2.valueAction_(3) and setting element1.index_(1) element1.value should return 3");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		element1.valueAction_(6);
		element1.widget_(widget2);
		this.assert(element1.widget === widget2, "After calling widget_ on the SlidersPerGroupNumberBox with arg 'widget' set to widget2 the SlidersPerGroupNumberBox's 'widget' getter should return widget2");
		this.assertEquals(element1.view.value, 1, "After setting element1.valueAction_(6) should leave element1's value at 1 after calling element1.widget_(widget2).");
		widget2.remove;
	}

	test_close {
		element1.close;
		this.assertEquals(widget1.syncKeys, [\default, element1.class.asSymbol], "After closing element1 widget1.syncKeys should hold two value: 'default' and '%'".format(element1.class.asSymbol));
		element2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing element2 widget1.syncKeys should hold one remaining value: 'default'");
	}
}

TestMidiInitButton : UnitTest {
	var button1, button2;

	setUp {
		button1 = MidiInitButton.new;
		button2 = MidiInitButton.new;
	}

	tearDown {
		button1.close;
		button2.close;
	}

	test_new {
		this.assertEquals(MidiInitButton.all.size, 2, "After creating 2 MidiInitButton instances MidiInitButton.all should have a size of 2");
		this.assertEquals(CVWidget.syncKeys, [\default, MidiInitButton.asSymbol], "CVWidget.syncKeys should hold two values after creating a MidiInitButton instance: 'default'  and 'MidiInitButton'");
	}

	test_click {
		this.ifAsserts(MIDIClient.initialized.not, "MIDIClient not initialized, test before init", {
			this.assertEquals(button1.view.states, [["init MIDI", Color(), Color.green]], "button1.view.states should equal [[\"reinit MIDI\", Color.white, Color.red]] if MIDIClient has not yet been initialized");
			this.assertEquals(button2.view.states, [["init MIDI", Color(), Color.green]], "button2.view.states should equal [[\"reinit MIDI\", Color.white, Color.red]] if MIDIClient has not yet been initialized");
		}, {
			this.assertEquals(button1.view.states, [["reinit MIDI", Color.white, Color.red]], "button1.view.states should equal [[\"reinit MIDI\", Color.white, Color.red]] if MIDIClient has already been initialized");
			this.assertEquals(button2.view.states, [["reinit MIDI", Color.white, Color.red]], "button2.view.states should equal [[\"reinit MIDI\", Color.white, Color.red]] if MIDIClient has already been initialized");
		});
		button1.doAction;
		this.assertEquals(button1.view.states, [["reinit MIDI", Color.white, Color.red]], "after clicking button1 button1.view.states should equal [[\"reinit MIDI\", Color.white, Color.red]]");
		this.assertEquals(button2.view.states, [["reinit MIDI", Color.white, Color.red]], "after clicking button1 button2.view.states should equal [[\"reinit MIDI\", Color.white, Color.red]] as well");
	}

	test_close {
		button1.close;
		this.assertEquals(button1.class.all, List[button2], "After closing button1 the 'all' variable should hold a list with a singel value: button2");
		this.assertEquals(CVWidget.syncKeys, [\default, button1.class.asSymbol], "After closing button1 CVWidget.syncKeys should hold two values: 'default' and '%'.".format(button1.class.asSymbol));
		button2.close;
		this.assertEquals(button2.class.all, List[], "After closing button2 the 'all' variable should hold an empty list");
		this.assertEquals(CVWidget.syncKeys, [\default], "After closing button1 CVWidget.syncKeys should hold a single value: 'default'.");
	}
}

TestMidiConnectorRemoveButton : UnitTest {
	var widget1, widget2, button1, button2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		button1 = MidiConnectorRemoveButton.new(widget: widget1);
		button2 = MidiConnectorRemoveButton.new(widget: widget1);
	}

	tearDown {
		button1.close;
		button2.close;
		widget1.remove;
	}

	test_new {
		this.assertEquals(MidiConnectorRemoveButton.all[widget1].size, 2, "After creating 2 MidiConnectorRemoveButton instances MidiInitButton.all[widget1] should have a size of 2");
	}

	test_index_ {
		this.assertEquals(widget1.wmc.midiConnectors.m.value[0].name, 'MIDI Connection 1', "After creating a CVWidgetKnob it should hold one MidiConnector named 'MIDI Connection 1'");
		widget1.addMidiConnector;
		button1.index_(1);
		this.assertEquals(button1.connector.name, 'MIDI Connection 2', "After adding another MidiConnector to the CVWidgetKnob instance stored in widget and setting calling index_ with a value 1 on button1 button1.connector.name should return 'MIDI Connection 2'");
		this.assertEquals(button2.connector.name, 'MIDI Connection 1', "After adding another MidiConnector to the CVWidgetKnob instance stored in widget and setting calling index_ with a value 1 on button1 button2.connector.name should return 'MIDI Connection 1'");
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		button1.widget_(widget2);
		this.assert(button1.widget === widget2, "After calling widget_ on the MidiConnectorRemoveButton with arg 'widget' set to widget2 the MidiConnectorRemoveButton's 'widget' getter should return widget2");
		widget2.remove;
	}

	test_click {
		widget1.addMidiConnector;
		button1.index_(1);
		button2.doAction;
		this.assertEquals(widget1.wmc.midiConnectors.m.value.size, 1, "After adding another MidiConnector to the CVWidgetKnob stored in widget and clicking button2 widget1.wmc.midiConnectors.m.value.size should return 1");
		this.assertEquals([button1.connector.name, button2.connector.name], ['MIDI Connection 2', 'MIDI Connection 2'], "After adding another MidiConnector to the CVWidgetKnob stored in widget and clicking button2 both, button1's and button2's connectors, should hold the MidiConnector named 'MIDI Connection 2' as connector though index_(1) has only been called on button1");
	}

	test_close {
		button1.close;
		this.assertEquals(button1.class.all[widget1], List[button2], "After closing button1 the 'all' variable at key widget1 should hold a list with a single value: button2");
		button2.close;
		this.assertEquals(button2.class.all[widget1], List[], "After closing button2 the 'all' variable should hold an empty list");
	}
}