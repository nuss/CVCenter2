OscConnectionEditorView : CompositeView {
	var <widget, <parent, <mc;
	// GUI elements
	var connectionSelect, addButton, removeButton;
	var ipSelect, restrictToPortCheckBox;
	var deviceSelect, oscCmdSelect, newDeviceBut;
	var oscCmdTextField, oscCmdSlotNumBox;
	var inputConstraintsLoNumBox, inputConstraintsHiNumBox, zeroCrossCorrectStaticText;
	var calibrationButton, resetButton;
	var specConstraintsStaticText, inOutMappingSelect, connectionButton;
	// var more, more...

	// FIXME: an OscConnection is a single connection
	// yet, a selection of connections can only belong
	// to the widget
	// -> must be considered when adding the model to the widget's
	// model - maybe the model can't be kept with the view?
	*new { |widget, parent|
		^super.new.init(widget, parent.asView);
	}

	init { |wdgt, parentView|
		widget = wdgt;
		parentView ?? {
			parent = Window("%: OSC connections".format(widget.name), Rect(0, 0, 300, 300))
		};
		// Just create a new OscConnection if none exists
		// it will automatically be added to the widget's list
		// of OscConnections within OscConnection:-init - should
		// also update the connectionSelect's items
		if (widget.oscConnections.isEmpty) {
			OscConnection(widget)
		};
		mc = widget.oscConnections;
		parent.layout_(
			VLayout(
				HLayout(
					connectionSelect = PopUpMenu(this),
					addButton = Button(this).states_([["+"]]),
					removeButton = Button(this).states_([["-"]])
				),
				HLayout(
					ipSelect = PopUpMenu(this),
					StaticText(this).string_("restrict to port"),
					restrictToPortCheckBox = CheckBox(this)
				),
				StaticText(this).string_("OSC command name - either select from list provided by the selected device or set custom one"),
				HLayout(
					deviceSelect = PopUpMenu(this),
					oscCmdSelect = PopUpMenu(this),
					newDeviceBut = Button(this)
				),
				HLayout(
					oscCmdTextField = TextField(this),
					oscCmdSlotNumBox = NumberBox(this)
				),
				StaticText(this).string_("OSC input constraints, zero-crossing correction"),
				HLayout(
					inputConstraintsLoNumBox = NumberBox(this),
					inputConstraintsHiNumBox = NumberBox(this),
					zeroCrossCorrectStaticText = StaticText(this),
					calibrationButton = Button(this),
					resetButton = Button(this),
				),
				specConstraintsStaticText = StaticText(this).string_("current widget spec constraints (lo/hi): 0/0"),
				StaticText(this).string_("input to output mapping"),
				inOutMappingSelect = PopUpMenu(this),
				connectionButton = Button(this)
			)
		);
	}

	front {
		parent.front;
	}
}

MidiConnectionEditorView : CompositeView {
	var <widget;
	// GUI elements
	var connectionSelect;

	*new { |widget, parent, bounds|
		^super.newCopyArgs(widget).init(parent.asView, bounds.asRect);
	}

	init {}
}