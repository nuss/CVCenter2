TestConnectorElementView : UnitTest {
	var widget1, widget2, element1, element2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		element1 = ConnectorNameField(widget: widget1, connectorKind: \midi);
		element2 = ConnectorNameField(widget: widget1, connectorKind: \osc);
	}

	tearDown {
		element1.close;
		element2.close;
		widget1.remove;
	}
}

TestConnectorNameField : UnitTest {
	var widget1, widget2;
	var midielement1, midielement2;
	var oscelement1, oscelement2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		midielement1 = ConnectorNameField(widget: widget1, connectorKind: \midi);
		oscelement1 = ConnectorNameField(widget: widget1, connectorKind: \osc);
	}

	tearDown {
		midielement1.close;
		oscelement1.close;
		widget1.remove;
	}

	test_new {
		this.assertEquals(ConnectorNameField.all[widget1][\midi][0], midielement1, "ConnectorNameField's 'all' variable after creation of a ConnectorNameField with 'connectorKind' set to 'midi' should hold a List with one value: the element itself.");
		this.assertEquals(ConnectorNameField.all[widget1][\osc][0], oscelement1, "ConnectorNameField's 'all' variable after creation of a ConnectorNameField with 'connectorKind' set to 'osc' should hold a List with one value: the element itself.");
		this.assertEquals(widget1.syncKeys, [\default, (\midi ++ ConnectorNameField.asString).asSymbol, (\osc ++ ConnectorNameField.asString).asSymbol], "The widget's 'syncKeys' should contain three Symbols, 'default', 'midiConnectorNameField' and 'oscMidiConnectorNameField' after creating a ConnectorNameField with 'connectorKind' 'midi' and a ConnectorNameField with 'connectorKind' 'osc'");
		this.assert(midielement1.connector.class == MidiConnector, "A ConnectorNameField created with 'connectorKind' 'midi' should hold a connector of class 'MidiConnector' after creation.");
		this.assert(oscelement1.connector.class == OscConnector, "A ConnectorNameField created with 'connectorKind' 'osc' should hold a connector of class 'OscConnector' after creation.");
		this.assert(midielement1.connector === widget1.wmc.midiConnectors.m.value[0], "The elements connector should be identical with the connector at the widget's midiConnectors List at index 0");
		this.assert(oscelement1.connector === widget1.wmc.oscConnectors.m.value[0], "The elements connector should be identical with the connector at the widget's oscConnectors List at index 0");
		midielement1.view.valueAction_('new midi name');
		oscelement1.view.valueAction_('new osc name');
		this.assertEquals(widget1.wmc.midiConnectorNames.m.value, List['new midi name'], "After calling 'valueAction' on the midi element's view the widget's midiConnectorNames model should hold a List with one value 'new midi name'");
		this.assertEquals(widget1.wmc.oscConnectorNames.m.value, List['new osc name'], "After calling 'valueAction' on the osc element's view the widget's oscConnectorNames model should hold a List with one value 'new osc name'");
		this.assertEquals(widget1.wmc.midiConnectors.m.value[0].name, 'new midi name', "widget1.wmc.midiConnectors.m.value[0].name should return 'new midi name' after  calling midielement1.view.valueAction_('new midi name')");
		this.assertEquals(widget1.wmc.oscConnectors.m.value[0].name, 'new osc name', "widget1.wmc.oscConnectors.m.value[0].name should return 'new osc name' after  calling midielement1.view.valueAction_('new osc name')");
		midielement2 = ConnectorNameField(widget: widget1, connectorKind: \midi);
		oscelement2 = ConnectorNameField(widget: widget1, connectorKind: \osc);
		this.assertEquals(ConnectorNameField.all[widget1][\midi].size, 2, "ConnectorNameField.all[widget1]['midi'] should hold a List with 2 elements after creating another ConnectorNameField instance.");
		this.assertEquals(midielement2.view.string, "new midi name", "midielement2's textfield should hold a string 'new midi name'");
		this.assertEquals(ConnectorNameField.all[widget1][\osc].size, 2, "ConnectorNameField.all[widget1]['osc'] should hold a List with 2 elements after creating another ConnectorNameField instance.");
		this.assertEquals(oscelement2.view.string, "new osc name", "oscelement2's textfield should hold a string 'new osc name'");
		midielement2.view.valueAction_('changed midi name');
		oscelement2.view.valueAction_('changed osc name');
		this.assertEquals(midielement1.view.string, "changed midi name", "After calling valueAction on midielement2's view with a value 'changed midi name' midielement1's view should have been updated accordingly");
		this.assertEquals(oscelement1.view.string, "changed osc name", "After calling valueAction on midielement2's view with a value 'changed osc name' midielement1's view should have been updated accordingly");
		midielement2.connector.name_("midi");
		this.assert(midielement1.string == "midi" and:{ midielement2.string == "midi" }, "After calling midielement2.connector.name_(\"midi\") both, midielement1's and midielement2's string should have been set to \"midi\".");
		oscelement2.connector.name_("osc");
		this.assert(oscelement1.string == "osc" and:{ oscelement2.string == "osc" }, "After calling midielement2.connector.name_(\"osc\") both, midielement1's and midielement2's string should have been set to \"osc\".");
		midielement2.close;
		oscelement2.close;
	}

	test_index_ {
		widget1.addMidiConnector;
		widget1.addOscConnector;
		midielement2 = ConnectorNameField(widget: widget1, connectorID: 1, connectorKind: \midi);
		oscelement2 = ConnectorNameField(widget: widget1, connectorID: 1, connectorKind: \osc);
		this.assertEquals(midielement2.connector, widget1.wmc.midiConnectors.m.value[1], "On instantiation the new ConnectorNameField's (connectorKind: 'midi') connector should be widget1.wmc.midiConnectors.m.value[1].");
		this.assertEquals(midielement2.view.string, "MIDI Connection 2", "After executing midielement2.index_(1) midielement2's view should hold a string 'MIDI Connection 2'");
		midielement1.index_(1);
		midielement2.view.valueAction_("another midi name");
		this.assertEquals(midielement1.view.string, "another midi name", "After calling midielement2.view.valueAction_(\"another midi name\") midielement1's view string should have been set to \"another midi name\"");
		this.assertEquals(oscelement2.connector, widget1.wmc.oscConnectors.m.value[1], "On instantiation the new ConnectorNameField's (connectorKind: 'osc') connector should be widget1.wmc.oscConnectors.m.value[1].");
		this.assertEquals(oscelement2.view.string, "OSC Connection 2", "After executing oscelement2.index_(1) oscelement2's view should hold a string 'OSC Connection 2'");
		oscelement1.index_(1);
		oscelement2.view.valueAction_("another osc name");
		this.assertEquals(oscelement1.view.string, "another osc name", "After calling oscelement2.view.valueAction_(\"another osc name\") oscelement1's view string should have been set to \"another osc name\"");
		midielement2.close;
		oscelement2.close;
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		widget2.wmc.midiConnectors.m.value[0].name_(\midi);
		widget2.wmc.oscConnectors.m.value[0].name_(\osc);
		midielement1.widget_(widget2);
		oscelement1.widget_(widget2);
		this.assert(midielement1.widget === widget2, "After calling widget_ on the ConnectorNameField (connectorKind: 'midi') with arg 'widget' set to widget2 the ConnectorNameField's 'widget' getter should returm widget2");
		this.assertEquals(midielement1.string.asSymbol, widget2.wmc.midiConnectorNames.m.value[0], "The ConnectorNameField's (connectorKind: 'midi') TextField should have been set to the name of the currently set value in widget2.wmc.midiConnectorNames.m.value[0]");
		this.assert(oscelement1.widget === widget2, "After calling widget_ on the ConnectorNameField (connectorKind: 'osc') with arg 'widget' set to widget2 the ConnectorNameField's 'widget' getter should returm widget2");
		this.assertEquals(oscelement1.string.asSymbol, widget2.wmc.oscConnectorNames.m.value[0], "The ConnectorNameField's (connectorKind: 'osc') TextField should have been set to the name of the currently set value in widget2.wmc.oscConnectorNames.m.value[0]");
		widget2.remove;
	}

	test_close {
		midielement1.close;
		oscelement1.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing midielement1 and oscelement1 widget1.syncKeys should hold one remaining value: 'default'.");
	}
}

TestConnectorSelect : UnitTest {
	var widget1, widget2;
	var midielement1, midielement2;
	var oscelement1, oscelement2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		midielement1 = ConnectorSelect(widget: widget1, connectorKind: \midi);
		oscelement1 = ConnectorSelect(widget: widget1, connectorKind: \osc);
	}

	tearDown {
		midielement1.close;
		oscelement1.close;
		widget1.remove;
	}

	test_new {
		this.assert(ConnectorSelect.all[widget1][\midi][0] === midielement1, "ConnectorSelect's (connectorKind: 'midi') all variable at the key which is the widget itself should hold a List with one value in its 'midi' slot: the element itself.");
		this.assert(ConnectorSelect.all[widget1][\osc][0] === oscelement1, "ConnectorSelect's (connectorKind: 'osc') all variable at the key which is the widget itself should hold a List with one value in its 'osc' slot: the element itself.");
		this.assertEquals(widget1.syncKeys, [\default, (\midi ++ ConnectorSelect.asString).asSymbol, (\osc ++ ConnectorSelect.asString).asSymbol], "The widget's 'syncKeys' should contain  three Symbols, 'default', 'midiConnectorSelect' and 'oscConnectorSelect', after creating a new ConnectorSelect with connectorKind 'midi' and another one with connectorKind 'osc'.");
		this.assert(midielement1.connector === widget1.wmc.midiConnectors.m.value[0], "The element's connector (midi) should be identical with the connector at the widget's midiConnectors List");
		this.assert(oscelement1.connector === widget1.wmc.oscConnectors.m.value[0], "The element's connector (osc) should be identical with the connector at the widget's oscConnectors List");
		this.assertEquals(widget1.wmc.midiConnectorNames.m.value, List['MIDI Connection 1'], "The widget's midiConnectorNames model should hold a List with one value 'MIDI Connection 1'");
		this.assertEquals(widget1.wmc.oscConnectorNames.m.value, List['OSC Connection 1'], "The widget's oscConnectorNames model should hold a List with one value 'OSC Connection 1'");
		this.assertEquals(midielement1.view.items, ['MIDI Connection 1', 'add MidiConnector...'], "The ConnectorSelect's (connectorKind: 'midi') items should hold two items: ['MIDI Connection 1', 'add MidiConnector...']");
		this.assertEquals(oscelement1.view.items, ['OSC Connection 1', 'add OscConnector...'], "The ConnectorSelect's (connectorKind: 'osc') items should hold two items: ['OSC Connection 1', 'add OscConnector...']");
		midielement2 = ConnectorSelect(widget: widget1, connectorKind: \midi);
		oscelement2 = ConnectorSelect(widget: widget1, connectorKind: \osc);
		this.assertEquals(ConnectorSelect.all[widget1][\midi].size, 2, "ConnectorSelect.all[widget1]['midi'] should hold a List with 2 elements after creating another ConnectorSelect (connectorKind: 'midi') instance.");
		this.assertEquals(ConnectorSelect.all[widget1][\osc].size, 2, "ConnectorSelect.all[widget1]['osc'] should hold a List with 2 elements after creating another ConnectorSelect (connectorKind: 'osc') instance.");
		midielement1.connector.name_("xyz_midi");
		this.assertEquals(midielement1.item, \xyz_midi, "After calling midielement1.connector.name_(\"xyz_midi\") midielement1.item should return 'xyz_midi'");
		this.assertEquals(midielement2.item, \xyz_midi, "After calling midielement1.connector.name_(\"xyz_midi\") midielement2.item should return 'xyz_midi'");
		oscelement1.connector.name_("xyz_osc");
		this.assertEquals(oscelement1.item, \xyz_osc, "After calling oscelement1.connector.name_(\"xyz_osc\") oscelement1.item should return 'xyz_osc'");
		this.assertEquals(oscelement2.item, \xyz_osc, "After calling oscelement1.connector.name_(\"xyz_osc\") oscelement2.item should return 'xyz_osc'");

		// can't test menu entries here as synchronisation of elements after changing select is
		// handled in MidiConnectorsEditorView:-init
		midielement2.close;
		oscelement2.close;
	}

	test_index_ {
		widget1.addMidiConnector;
		widget1.addOscConnector;
		midielement2 = ConnectorSelect(widget: widget1, connectorID: 1, connectorKind: \midi);
		oscelement2 = ConnectorSelect(widget: widget1, connectorID: 1, connectorKind: \osc);
		this.assert(midielement2.connector === widget1.wmc.midiConnectors.m.value[1], "After creating a new ConnectorSelect (connectorKind: 'midi') with connectorID set to 1 the ConnectorSelect's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		this.assertEquals(midielement2.view.value, 1, "midielement2.value should return 1.");
		midielement2.connector.name_("aaaaaa_midi");
		this.assertEquals(midielement1.items[1], \aaaaaa_midi, "After calling midielement2.connector.name_(\"aaaaaa_midi\") midielement1.items[1] should return 'aaaaaa_midi'.");
		this.assert(oscelement2.connector === widget1.wmc.oscConnectors.m.value[1], "After creating a new ConnectorSelect (connectorKind: 'osc') with connectorID set to 1 the ConnectorSelect's connector should be identical with widget1.wmc.midiConnectors.m.value[1]");
		this.assertEquals(oscelement2.view.value, 1, "oscelement2.value should return 1.");
		oscelement2.connector.name_("aaaaaa_osc");
		this.assertEquals(oscelement1.items[1], \aaaaaa_osc, "After calling oscelement2.connector.name_(\"aaaaaa_osc\") oscelement1.items[1] should return 'aaaaaa_osc'.");
		midielement2.close;
		oscelement2.close;
	}

	test_widget_ {
		widget2 = CVWidgetKnob(\test2);
		widget2.wmc.midiConnectors.m.value[0].name(\qqqqqq_midi);
		widget2.wmc.oscConnectors.m.value[0].name(\qqqqqq_osc);
		midielement1.widget_(widget2);
		oscelement1.widget_(widget2);
		this.assert(midielement1.widget === widget2, "After calling widget_ on the ConnectorSelect (connectorKind: 'midi') with arg 'widget' set to widget2 the ConnectorSelect's 'widget' getter should return widget2");
		this.assert(oscelement1.widget === widget2, "After calling widget_ on the ConnectorSelect (connectorKind: 'osc') with arg 'widget' set to widget2 the ConnectorSelect's 'widget' getter should return widget2");
		widget2.remove;
	}

	test_close {
		midielement1.close;
		this.assertEquals(widget1.syncKeys, [\default, \oscConnectorSelect], "After closing midielement1 widget1.syncKeys should hold two remaining values: 'default' and 'oscConnectorSelect'.");
		oscelement1.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing oscelement1 widget1.syncKeys should hold one remaining value: 'default'.");
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
		this.assertEquals(element1.view.value, 63, "After setting element1.valueAction_(60) should leave element1's value at 63 after calling element1.widget_(widget2).");
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
		MIDIClient.disposeClient;
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
		button1 = ConnectorRemoveButton(widget: widget1, connectorKind: \midi);
		button2 = ConnectorRemoveButton(widget: widget1, connectorKind: \midi);
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