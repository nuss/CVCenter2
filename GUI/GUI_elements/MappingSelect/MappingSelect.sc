MappingSelect : CompositeView {
	classvar all;
	var mc;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, connectorKind|
		^super.new.init(parent, widget, rect, connectorID, connectorKind);
	}

	init { |parentView, wdgt, rect, index, connectorKind|
		all[widget] ?? {
			all.put(widget, List[])
		};
		all[widget].add(this);
		connectorKind {
			Error("arg connectorKind in MappingPlot.new must either be 'midi' or 'osc'!").throw
		};
		connectorKind = connectorKind.asSymbol;
		// mc = ? // which model should this refer to???
	}

}