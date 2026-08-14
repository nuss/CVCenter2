ConnectorElementView : SCViewHolder {
	// the widget, the slot - if widget is a CVWidgetMS, otherwise nil
	// initialized through super.newCopyArgs
	var <widget, <slot;
	// the connector (not the ID but the object)
	var <connector;
	// TODO: should be local
	var mc, conModel;
	// models, controllers, differentiated by their widget's class'
	var mcM, mcC;
	var conModelM, conModelC;
	// specific to every element
	var syncKey;
	// could be local but doesn't have to
	var toolTip;

	close {
		this.remove;
		this.viewDidClose;
		this.prCleanup;
	}

	prCleanup {
		this.class.all[this.widget].remove(this);

		// TODO: take care of CVWidgetKnob and CVWidgetMS separation

		if (this.class.all[this.widget].isEmpty) {
			// FIXME: a remnant from earlier, obsolete considerations?
			// if (mc.size > 2) {
			// 	mc.do { |v|
			// 		"mc: %".format(v).postln;
			// 		switch (v.class)
			// 		{ Event } { v.c.removeAt(syncKey) }
			// 		// special case: oscInputConstrainters holds two CVs, \lo and \hi
			// 		{ List } { v.do(_.disconnect) }
			// 	}
			// } {
			// 	"mc.size: %, mc: %".format(mc.size, mc).postln;
			// 	mc.c.removeAt(syncKey);
			// };
			mc.c.removeAt(syncKey);
			this.widget.prRemoveSyncKey(syncKey, true);
		}
	}

	// suitable for all instances, hence widget must
	// be passed in explicitly within Osc/MidiConnector:-remove
	prOnRemoveConnector { |widget, index|
		if (index > 0) {
			this.class.all[widget].do(_.index_(index - 1))
		} {
			this.class.all[widget].do(_.index_(index))
		}
	}

	slot_ { |newSlot|
		if (this.class === CVWidgetMS) { slot = newSlot }
	}
}

ConnectorNameField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	// connectorKind - either \midi or \osc
	// initialized through super.newCopyArgs
	var connectorKind, cclass;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0), connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		// ^super.new.init(parent, widget, rect, connectorID, connectorKind)
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	// init { |parentView, wdgt, rect, index, kind|
	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnectorNames;
			conModel = widget.wmc.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnectorNames;
			conModel = widget.wmc.oscConnectors;
		};

		case
		{ widget.class === CVWidgetKnob } {
			mcM = mc.m;
			mcC = mc.c;
			conModel = conModel.m.value
		}
		{ widget.class === CVWidgetMS } {
			mcM = mc.m[this.slot];
			mcC = mc.c[this.slot];
			conModel = conModel.m[this.slot].value
		};

		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			// this.connector - an OscConnector or a MidiConnector set in index_
			this.connector.name_(tf.string.asSymbol)
		});
		this.view.onClose_({ this.close });
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			// FIXME: if widget is a CVWidgetMS likely a slot index must be given
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
		mcM.value !? {
			this.view.string_(mcM.value[connectorID])
		}
	}

	// setWidget { |otherWidget, slot|
	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnectorNames;
			conModel = widget.wmc.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnectorNames;
			conModel = widget.wmc.oscConnectors;
		};
		case
		{ widget.class === CVWidgetKnob } {
			mcM = mc.m;
			mcC = mc.c;
			conModel = conModel.m.value
		}
		{ widget.class === CVWidgetMS } {
			mcM = mc.m[this.slot];
			mcC = mc.c[this.slot];
			conModel = conModel.m[this.slot].value };
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;
		mcC ?? {
			mcC = SimpleController(mcM)
		};
		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		// if there's already a function defined for synKey simply replace it
		mcC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |tf|
				if (tf.connector === conModel[conID]) {
					tf.view.string_(changer.value[conID]);
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				mcC.removeAt(syncKey);
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}

}

ConnectorSelect : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var cons, connectorKind, cclass;
	var consM, consC;
	var selItem0;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0), connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		// ^super.new.init(parent, widget, rect, connectorID, connectorKind);
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnectorNames;
			cons = widget.wmc.midiConnectors;
			selItem0 = 'add MidiConnector...'
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnectorNames;
			cons = widget.wmc.oscConnectors;
			selItem0 = 'add OscConnector...'
		};

		case
		{ widget.class === CVWidgetKnob } {
			mcM = mc.m;
			mcC = mc.c;
			consM = cons.m;
			consC = cons.c;
		}
		{ widget.class === CVWidgetMS } {
			mcM = mc.m[this.slot];
			mcC = mc.c[this.slot];
			consM = cons.m[this.slot];
			consC = cons.c[this.slot];
		};

		this.view = PopUpMenu(parentView)
		.items_(mcM.value ++ [selItem0]);
		this.view.onClose_({ this.close });
		this.index_(index);
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = consM.value[connectorID];
		this.view.value_(connectorID);
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};

		all[otherWidget][connectorKind].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnectorNames;
			cons = widget.wmc.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnectorNames;
			cons = widget.wmc.oscConnectors;
		};

		case
		{ widget.class === CVWidgetKnob } {
			mcM = mc.m;
			mcC = mc.c;
			consM = cons.m
		}
		{ widget.class === CVWidgetMS } {
			mcM = mc.m[this.slot];
			mcC = mc.c[this.slot];
			consM = cons.m[this.slot];
			consC = cons.c[this.slot];
		};

		this.view.items_(mcM.value ++ this.view.items.last);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var items, conID;
		var curValue;
		mcC ?? {
			mcC = SimpleController(mcM)
		};
		consC ?? {
			consC = SimpleController(consM)
		};
		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		consC.put(syncKey, { |changer, what ... moreArgs|
			all[widget][connectorKind].do { |sel, i|
				curValue = sel.view.value;
				sel.view.items_(mcM.value ++ sel.view.items.last)
				.value_(curValue);
			}
		});
		mcC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |sel, i|
				items = sel.view.items;
				items[conID] = changer.value[conID];
				curValue = sel.view.value;
				sel.view.items_(items).value_(curValue);
				if (sel.connector === consM.value[conID]) {
					sel.view.value_(conID)
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				mcC.removeAt(syncKey);
				consC.removeAt(syncKey);
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

ConnectorRemoveButton : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var connectorKind, cclass;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0), connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		// ^super.new.init(parent, widget, rect, connectorID, connectorKind);
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			conModel = widget.wmc.midiConnectors;
		}
		{ connectorKind === \osc } {
			conModel = widget.wmc.oscConnectors;
		};

		case
		{ widget.class === CVWidgetKnob } {
			conModel = conModel.m.value
		}
		{ widget.class === CVWidgetMS } {
			conModel = conModel.m[this.slot].value
		};

		this.index_(index);
		this.view = Button(parentView, rect)
		.states_([["remove Connector", Color.white, Color(0, 0.5, 0.5)]])
		.action_({ this.connector.remove });
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		}
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
	}

	widget_ { |otherWidget, slot|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);

		case
		{ connectorKind === \midi } {
			conModel = widget.wmc.midiConnectors;
		}
		{ connectorKind === \osc } {
			conModel = widget.wmc.oscConnectors;
		};

		case
		{ widget.class === CVWidgetKnob } {
			conModel = conModel.m.value
		}
		{ widget.class === CVWidgetMS } {
			conModel = conModel.m[this.slot].value
		};

		this.index_(0);
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

// displays current ControlSpec,
// independent from MIDI and OSC,
// resp., current connector
ControlSpecText : ConnectorElementView {
	classvar <all;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget).init(parent, rect)
	}

	init { |parentView, rect|
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.cvSpec;

		this.view = StaticText(parentView, rect)
		.string_("Current ControlSpec:\n%".format(mc.m.value));
		this.view.onClose_({ this.close });
		this.prAddController;
	}

	index_ {}

	widget_ { |otherWidget|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		mc = widget.wmc.cvSpec;
		this.prAddController;
	}

	prAddController {
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			all[widget].do { |txt|
				defer { txt.string_("Current ControlSpec:\n%".format(changer.value)) }
			}
		})
	}
}

TemplateTextField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var connectorKind;
	var cclass, connections;
	var connectionsM, connectionsC;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID=0, connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		// ^super.new.init(parent, widget, rect, connectorID, connectorKind);
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	init { |parentView, rect, index|
		var conID, action;

		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiDisplay;
			conModel = widget.wmc.midiConnectors;
			connections = widget.wmc.midiConnections;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscDisplay;
			conModel = widget.wmc.oscConnectors;
			connections = widget.wmc.oscConnections;
		};

		case
		{ widget.class === CVWidgetKnob } {
			mcM = mc.m;
			mcC = mc.c;
			conModel = conModel.m.value;
			connectionsM = connections.m;
			connectionsC = connections.c;
		}
		{ widget.class === CVWidgetMS } {
			mcM = mc.m[this.slot];
			conModel = conModel.m[this.slot].value;
			connectionsM = connections.m[this.slot];
			connectionsC = connections.c[this.slot];
		};

		this.view = TextView(parentView)
		.string_(mcM.value[index].template.cs)
		.syntaxColorize
		.font_(Font.monospace);
		this.view.onClose_({ this.close });
		this.index_(index);
		action = { |tv|
			conID = connector.index;
			if (tv.string.size > 0) {
				mcM.value[conID].template = tv.string.interpret;
			} {
				mcM.value[conID].template = nil;
			};
			mcM.changedPerformKeys(widget.syncKeys, conID);
		};
		this.view.action_(action);
		this.view.focusLostAction_(action);
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
		this.view.string_(mcM.value[connectorID].template)
		.editable_(connectionsM.value[connectorID].isNil);
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiDisplay;
			conModel = widget.wmc.midiConnectors;
			connections = widget.wmc.midiConnections;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscDisplay;
			conModel = widget.wmc.oscConnectors;
			connections = widget.wmc.oscConnections;
		};

		case
		{ widget.class === CVWidgetKnob } {
			mcM = mc.m;
			mcC = mc.c;
			conModel = conModel.m.value;
			connectionsM = connections.m;
			connectionsC = connections.c;
		}
		{ widget.class === CVWidgetMS } {
			mcM = mc.m[this.slot];
			mcC = mc.c[this.slot];
			conModel = conModel.m[this.slot].value;
			connectionsM = connections.m[this.slot];
			connectionsC = connections.c[this.slot];
		};
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;

		mcC ?? {
			mcC = SimpleController(mcM)
		};
		connectionsC ?? {
			connectionsC = SimpleController(connectionsM)
		};
		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mcC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |tv|
				if (tv.connector === conModel[conID]) {
					defer { tv.view.string_(mcM.value[conID].template) }
				}
			}
		});
		connectionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |tv|
				if (tv.connector === conModel[conID]) {
					defer { tv.view.enabled_(connectionsM.value[conID].isNil) }
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				mcC.removeAt(syncKey);
				connections.c.removeAt(syncKey);
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

PlayPauseButton : ConnectorElementView {
	// inspired by https://scsynth.org/t/is-it-possible-to-make-a-round-button/7082/3
	classvar <all, connectorRemovedFuncAdded;
	var connectorKind;
	var cclass, <buttonLayout;
	var disabledBgColor, disabledFgColor;
	var enabledFgColor;
	var enabledBgColor; // array [0 = paused, 1 = playing]
	var funcClassName, enabledMethod, toolTips;

	*initClass {
		all = ()
	}

	*new { |parent, widget, rect, slot, connectorID=0, connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	init { |parentView, rect, index|
		var conID, action, buttonBgColor ;

		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		disabledBgColor = Color.gray(0.6);
		disabledFgColor = Color.gray(0.9);
		enabledBgColor = [Color.green, Color.red];
		enabledFgColor = Color.black;

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnections;
			conModel = widget.wmc.midiConnectors;
			funcClassName = "MIDIFunc";
			enabledMethod = \getMIDIFuncEnabled
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnections;
			conModel = widget.wmc.oscConnectors;
			funcClassName = "OSCFunc";
			enabledMethod = \getOSCFuncEnabled
		};

		case
		{ widget.class === CVWidgetKnob } {
			mcM = mc.m;
			mcC = mc.c;
			conModel = conModel.m.value;
		}
		{ widget.class === CVWidgetMS } {
			mcM = mc.m[this.slot];
			mcC = mc.c[this.slot];
			conModel = conModel.m[this.slot].value;
		};

		toolTips = [
			"Click to disable %".format(funcClassName),
			"Click to enable %".format(funcClassName)
		];

		buttonBgColor = if (mc.m.value[index].notNil) {
			enabledBgColor[mc.m.value[index].enabled.asInteger]
		} { Color.gray(0.6) };

		this.view = Button(parentView);
		this.view.states_([
			["", Color.clear, Color.clear],
			["", Color.clear, Color.clear]
		])
		.canFocus_(false)
		.layout_(
			HLayout(
				buttonLayout = UserView()
				.acceptsMouse_(false)
				.background_(buttonBgColor)
				.drawFunc_(this.prMakeLabelDrawFunc(
					mcM.value[index].notNil,
					mcM.value[index] !? { mcM.value[index].enabled }
				))
			)
			.margins_(0)
			.spacing_(0)
		)
		.enabled_(mcM.value[index].notNil)
		.action_({ |bt|
			if (connectorKind === \midi) {
				this.connector.setMIDIFuncEnabled(bt.value.asBoolean.not);
			} {
				this.connector.setOSCFuncEnabled(bt.value.asBoolean.not)
			};
			bt.toolTip_(toolTips[bt.value])
		})
		.maxWidth_(25);
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.index_(index);
		if (mcM.value[index].notNil) {
			this.view.toolTip_(toolTips[connector.perform(enabledMethod).not.asInteger])
		} {
			this.view.toolTip_("No % currently present".format(funcClassName))
		};
		this.prAddController;
	}

	prMakeLabelDrawFunc { |funcExists, enabled = false|
		var fgColor, bgColor, funcEnabled;
		var iconSize;

		if (this.view.bounds.width > this.view.bounds.height) {
			iconSize = this.view.bounds.height/2
		} {
			iconSize = this.view.bounds.width/2
		};

		if (funcExists) {
			fgColor = enabledFgColor;
			bgColor = enabledBgColor[enabled.asInteger]
		} {
			fgColor = disabledFgColor;
			bgColor = disabledBgColor;
		};

		case
		{ funcExists and: { enabled.not }} {
			^{ |v|
				Pen
				.fillColor_(fgColor)
				.moveTo(Point(this.view.bounds.width/2-(iconSize/2), this.view.bounds.height/2-(iconSize/2)))
				.lineTo(Point(this.view.bounds.width/2+(iconSize/2), this.view.bounds.height/2))
				.lineTo(Point(this.view.bounds.width/2-(iconSize/2), this.view.bounds.height/2+(iconSize/2)))
				.fill
			}
		}
		{ (funcExists.not).or(funcExists and: { enabled }) } {
			^{ |v|
				Pen
				.fillColor_(fgColor)
				.addRect(Rect(
					this.view.bounds.width/2-(iconSize/2),
					this.view.bounds.height/2-(iconSize/2),
					iconSize/5*2,
					iconSize
				))
				.addRect(Rect(
					this.view.bounds.width/2-(iconSize/2)+(iconSize/5*3),
					this.view.bounds.height/2-(iconSize/2),
					iconSize/5*2,
					iconSize
				)).fill
			}
		}
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
		if (mcM.value[connectorID].notNil) {
			buttonLayout.background_(enabledBgColor[mcM.value[connectorID].enabled.asInteger])
			.drawFunc_(this.prMakeLabelDrawFunc(true, mcM.value[connectorID].enabled)).refresh;
			this.view.enabled_(true);
		} {
			buttonLayout.background_(Color.gray(0.6))
			.drawFunc_(this.prMakeLabelDrawFunc(false)).refresh;
			this.view.enabled_(false);
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnections;
			conModel = widget.wmc.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnections;
			conModel = widget.wmc.oscConnectors;
		};

		case
		{ widget.class === CVWidgetKnob } {
			mcM = mc.m;
			mcC = mc.c;
			conModel = conModel.m.value;
		}
		{ widget.class === CVWidgetMS } {
			mcM = mc.m[this.slot];
			mcC = mc.c[this.slot];
			conModel = conModel.m[this.slot].value;
		};

		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;
		var funcEnabled;

		mcC ?? {
			mcC = SimpleController(mcM)
		};
		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mcC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |bt|
				if (bt.connector === conModel[conID]) {
					switch (connectorKind)
					{ \midi } { funcEnabled = conModel[conID].getMIDIFuncEnabled }
					{ \osc } { funcEnabled = conModel[conID].getOSCFuncEnabled };
					if (mc.m.value[conID].notNil) {
						defer {
							bt.buttonLayout
							.background_(enabledBgColor[funcEnabled.asInteger])
							.drawFunc_(bt.prMakeLabelDrawFunc(true, funcEnabled)).refresh;
							bt.enabled_(true).toolTip_(toolTips[bt.connector.perform(enabledMethod).not.asInteger])
						}
					} {
						defer {
							bt.buttonLayout
							.background_(disabledBgColor)
							.drawFunc_(bt.prMakeLabelDrawFunc(false)).refresh;
							bt.enabled_(false).toolTip_("No % currently present".format(funcClassName))
						}
					}
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				mcC.removeAt(syncKey);
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}
