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

	}

	test_set {

	}

	test_widget_ {

	}

	test_close {

	}

	test_closeAll {

	}
}