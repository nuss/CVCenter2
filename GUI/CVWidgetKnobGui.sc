CVWidgetKnobGui : AbstractCVWidgetGui {

	*new { |window, widget|
		^super.newCopyArgs(window, widget).init;
	}

	init {
		var mc = widget.wmc;


	}

}