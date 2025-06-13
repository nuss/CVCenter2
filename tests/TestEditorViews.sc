TestMidiConnectorsEditorView : UnitTest {
	var widget, editor;

	setUp {
		widget = CVWidgetKnob(\test);
		editor = MidiConnectorsEditorView(widget);
	}

	tearDown {
		editor.close;
		widget.remove;
	}

	test_new {
		var connectorsSet;

		this.assertEquals(editor.widget, widget, "After creating a new MidiConnectorsEditorView its 'widget' variable should equal widget.");
		editor.close;
		editor = MidiConnectorsEditorView(widget, widget.midiConnectors[0]);
		this.assertEquals(editor.widget, widget, "After creating a new MidiConnectorsEditorView, explicitly passing in widget.midiConnectors[0] in arg 'connector', its 'widget' variable should equal widget.");
		editor.close;
		editor = MidiConnectorsEditorView(widget, 'MIDI Connection 1');
		this.assertEquals(editor.widget, widget, "After creating a new MidiConnectorsEditorView, explicitly passing the MidiConnector's name in arg 'connector', its 'widget' variable should equal widget.");
		editor.close;
		widget.removeMidiConnector(0, true);
		editor = MidiConnectorsEditorView(widget);
		this.assertEquals(widget.midiConnectors.size, 1, "After creating a new MidiConnectorsEditorView for a widget with an empty 'midiConnectors' array a new MidiConnector should have been added to widget.midiConnectors automatically.");
		connectorsSet = editor.e.reject({ |v, k| k === \midiInit }).collectAs(_.connector, Set);
		this.assertEquals(connectorsSet, Set[widget.midiConnectors[0]], "All GUI elements in editor should have been set to widget.midiConnectors[0] (except for the MIDI init button which doesn't depend on the connector).");
	}

	test_set {
		var connectorsSet;

		widget.addMidiConnector;
		editor.set(1);
		connectorsSet = editor.e.reject({ |v, k| k === \midiInit }).collectAs(_.connector, Set);
		this.assertEquals(connectorsSet, Set[widget.midiConnectors[1]], "All GUI elements in editor should have been set to widget.midiConnectors[1] after calling editor.set(1) (except for the MIDI init button which doesn't depend on the connector).");
		editor.set(widget.midiConnectors[0]);
		connectorsSet = editor.e.reject({ |v, k| k === \midiInit }).collectAs(_.connector, Set);
		this.assertEquals(connectorsSet, Set[widget.midiConnectors[0]], "All GUI elements in editor should have been set to widget.midiConnectors[0] after calling editor.set(widget.midiConnectors[0]) (except for the MIDI init button which doesn't depend on the connector).");
	}

	test_widget_ {
		var otherWidget = CVWidgetKnob(\other);
		var connectorsSet;

		editor.widget_(otherWidget);
		this.assertEquals(editor.connector, otherWidget.midiConnectors[0], "After calling editor.widget_(otherWidget) the editor's 'widget' variable should have been set to otherWidget.midiConnectors[0].");
		connectorsSet = editor.e.reject({ |v, k| k === \midiInit }).collectAs(_.connector, Set);
		this.assertEquals(connectorsSet, Set[otherWidget.midiConnectors[0]], "All GUI elements in editor should have been set to otherWidget.midiConnectors[0] after calling editor.widget_(otherWidget) (except for the MIDI init button which doesn't depend on the connector).");
		otherWidget.remove;
	}

	test_close {
		editor.close;
		this.assert(MidiConnectorsEditorView.all[widget].isEmpty, "After calling 'close' on the editor MidiConnectorsEditorView.all[widget] should be empty");
		editor.e.do { |el|
			switch (el.class)
			{ MappingSelect } {
				this.assert(MappingSelect.all[widget][\midi].isEmpty, "After calling 'close' on the editor MappingSelect.all[widget]['midi'] should be empty")
			}
			{ MidiInitButton } {
				this.assert(MidiInitButton.all.isEmpty, "After calling 'close' on the editor MidiInitButton.all should be empty")
			}
			{ this.assert(el.class.all[widget].isEmpty, "After calling 'close' on the editor %.all[widget] should be empty".format(el.class)) }
		}
	}

	test_closeAll {
		var anotherWidget = CVWidgetKnob(\anotherOne);
		var anotherEditor = MidiConnectorsEditorView(anotherWidget);
		var plusOneMore = MidiConnectorsEditorView(anotherWidget);

		MidiConnectorsEditorView.closeAll;
		MidiConnectorsEditorView.all { |list|
			this.assertEquals(list, List[], "After calling MidiConnectorsEditorView.closeAll all keys in MidiConnectorsEditorView.all should reference empty lists.")
		}
	}
}