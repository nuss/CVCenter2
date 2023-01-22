OscConnectorsEditorView : CompositeView {
	var <widget, <mc, <parent;
	// GUI elements
	var connectionSelect, addButton, removeButton;
	var ipSelect, restrictToPortCheckBox;
	var deviceSelect, oscCmdSelect, newDeviceBut;
	var oscCmdTextField, oscCmdSlotNumBox;
	var inputConstraintsLoNumBox, inputConstraintsHiNumBox, zeroCrossCorrectStaticText;
	var calibrationButton, resetButton;
	var specConstraintsStaticText, inOutMappingSelect, connectionButton;
	// var more, more...

	// FIXME: an OscConnector is a single connection
	// yet, a selection of connections can only belong
	// to the widget
	// -> must be considered when adding the model to the widget's
	// model - maybe the model can't be kept with the view?
	*new { |widget, parent|
		^super.new.init(widget, parent.asView);
	}

	init { |wdgt, parentView|
		widget = wdgt;
		if (parentView.isNil) {
			parent = Window("%: OSC connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };
		// Just create a new OscConnector if none exists
		// it will automatically be added to the widget's list
		// of OscConnectors within OscConnector:-init - should
		// also update the connectionSelect's items
		if (widget.oscConnectors.isEmpty) {
			OscConnector(widget)
		};
		mc = widget.oscConnectors;
		parent.layout_(
			VLayout(
				HLayout(
					connectionSelect = PopUpMenu(parent),
					addButton = Button(parent).states_([["+"]]),
					removeButton = Button(parent).states_([["-"]])
				),
				HLayout(
					ipSelect = PopUpMenu(parent),
					StaticText(parent).string_("restrict to port"),
					restrictToPortCheckBox = CheckBox(parent)
				),
				StaticText(parent).string_("OSC command name - either select from list provided by the selected device or set custom one"),
				HLayout(
					deviceSelect = PopUpMenu(parent),
					oscCmdSelect = PopUpMenu(parent),
					newDeviceBut = Button(parent)
				),
				HLayout(
					oscCmdTextField = TextField(parent),
					oscCmdSlotNumBox = NumberBox(parent)
				),
				StaticText(parent).string_("OSC input constraints, zero-crossing correction"),
				HLayout(
					inputConstraintsLoNumBox = NumberBox(parent),
					inputConstraintsHiNumBox = NumberBox(parent),
					zeroCrossCorrectStaticText = StaticText(parent),
					calibrationButton = Button(parent),
					resetButton = Button(parent),
				),
				specConstraintsStaticText = StaticText(parent).string_("current widget spec constraints (lo/hi): 0/0"),
				StaticText(parent).string_("input to output mapping"),
				inOutMappingSelect = PopUpMenu(parent),
				connectionButton = Button(parent)
			)
		);
	}

	front {
		parent.front;
	}
}

MidiConnectorsEditorView : CompositeView {
	var <widget, <mc, <parent;
	// GUI elements
	var connectionSelect;
	var midiModeSelect, midiMeanBox, softWithinBox, midiResolutionBox, slidersPerBankTF;


	*new { |widget, parent|
		^super.new.init(widget, parent.asView);
	}

	init { |wdgt, parentView|
		widget = wdgt;
		if (parentView.isNil) {
			parent = Window("%: MIDI connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };
		if (widget.midiConnectors.isEmpty) {
			MidiConnector(widget)
		};
		mc = widget.midiConnectors;
		parent.layout_(
			VLayout(
				HLayout(
					connectionSelect = ConnectionSelect(parent, widget).items_(["Select connection..."])
				),
				HLayout(
					StaticText(parent).string_("MIDI mode: 0-127 or in/decremental "),
					midiModeSelect = MidiModeSelect(parent, widget)
				),
				HLayout(
					StaticText(parent).string_("MIDI mean (in/decremental mode only): "),
					midiMeanBox = MidiMeanNumberBox(parent, widget)
				),
				HLayout(
					StaticText(parent).string_("min. snap distance for slider (0-127 only): "),
					softWithinBox = SoftWithinNumberBox(parent, widget)
				),
				HLayout(
					StaticText(parent).string_("MIDI resolution (+/- only): "),
					midiResolutionBox = MidiResolutionNumberBox(parent, widget)
				),
				HLayout(
					StaticText(parent).string_("Number of sliders per bank: "),
					slidersPerBankTF = SlidersPerBankNumberTF(parent, widget)
				)
			)
		)
	}

	front {
		parent.front;
	}


}