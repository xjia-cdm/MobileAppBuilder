
package xj.mobile.common

import java.awt.Font
import java.awt.Label
import java.awt.FontMetrics

import xj.mobile.model.ui.*
import xj.mobile.lang.*
import xj.mobile.model.properties.Font
import xj.mobile.util.FontMetrics

import static xj.mobile.util.CommonUtils.linesInString
import static xj.mobile.common.ViewUtils.isDependentAttribute
import static xj.mobile.common.ViewUtils.allowImage
import static xj.mobile.common.ViewUtils.getImageSizeForWidget
import static xj.mobile.common.ViewUtils.getPlatformWidgetName


import static java.lang.Math.max
import static xj.translate.Logger.info 

class Layout { 

  // return dimension of view [width, height] (the content size)
  static def layout(View view, 
					def config,
					boolean setWidgetFrame = true) { 
    info "[Layout] view id=${view.id} type=${view.widgetType}"
    int m = config.defaults.margin
	int statusBarHeight = config.defaults.statusBar.height
    int vw = 0, vh = 0
	boolean bounded = !(view.scroll || 
						view.children.any { w -> (w instanceof Widget) && w.scroll })
	vw = config.defaults.Screen.width - 2 * m
	vh = config.defaults.Screen.height - 2 * m
    if (view.nodeType == 'TabbedView') { 
      vh -= config.defaults.TabBar.height
    }
	def constraints = [ top : [ 'top', null, m ],
						left : [ 'left', null, m ], 
						right: [ 'right', null, m ], 
						bottom: [ 'bottom', null, m ], ]
	def dim = layout(view, m, m, // + statusBarHeight,
					 vw, vh, m, m, config, constraints, bounded, setWidgetFrame) 
	//if (view.autoLayout) layoutConstraint(view, config)
    return dim
  }

  // return [width, heght] (the content size)
  static layout(View view, 
				int x, int y,     // current position
				int rw, int rh,   // remaining width and height
				int mx, int my,   // margin x and y 
				def config,
				def constraints, 
				boolean bounded = true, 
				boolean setWidgetFrame = true) { 
    info "[Layout] layout(): id=${view.id} cur=(${x},${y}) remaining=(${rw},${rh})"

    if (view.nodeType == 'Table') { 
      return layoutTable(view, x, y, rw, rh, mx, my, config, constraints, bounded, setWidgetFrame)
    } 

    int vw = 0, vh = 0;
    if (view && view.children) { 
      boolean horizontal = (view.nodeType in [ 'Box', 'Row' ])
      if (view.orientation) { 
		horizontal = ('horizontal' == view.orientation)
      }

      int cx = x, cy = y
      int w = 0, h = 0
      boolean wfill = false, hfill = false 
      boolean topmost = true, leftmost = true
	  boolean inGroup = Language.isGroup(view.nodeType)
	  Widget prev = null
      view.children.each { widget -> 
		info "[Layout] ${view.id} child ${widget.id} isUI=${Language.isUI(widget.nodeType)} cur=(${cx},${cy}) remaining=(${rw},${rh}) size=(${vw},${vh})"

		if (Language.isTopView(widget.nodeType) && !widget.embedded) { 
		  layout(widget, config, setWidgetFrame)
		} else if (Language.isUI(widget.nodeType) &&
				   !Language.isPopup(widget.nodeType)) { 
		  if (constraints) { 
			widget['#layout'] = []
			if (topmost) widget['#layout'] << constraints.top 
			if (leftmost) widget['#layout'] << constraints.left 
		  }

		  if (Language.isGroup(widget.nodeType)) { 
			int rw0 = rw - (cx - x)
			int rh0 = rh - (cy - y)

			if (horizontal) { 
			  if (prev) constraints.left = [ 'next', prev.id, config.defaults.gap ] 
			} else { 
			  if (prev) { 
				if (Language.isGroup(prev.nodeType)) { 
				  constraints.top = [ 'below', prev.children.id, config.defaults.gap ]
				} else { 
				  constraints.top = [ 'below', prev.id, config.defaults.gap ]
				}
			  }
			}
			(w, h) = layout(widget, cx, cy, rw0, rh0, mx, my, config, constraints, bounded, setWidgetFrame)	    
		  } else if (Language.isTopView(widget.nodeType) && widget.embedded) {  
			(w, h) = [ rw, rh ]  // a group view will take the remaining space
			if (constraints) 
			  widget['#layout'] << [ 'bottom', null, mx ] << [ 'right', null, my ]
		  } else { 
			if (widget.frame) { 
			  (cx, cy, w, h) = widget.frame
			} else { 
			  (cx, cy, w, h, wfill, hfill) = determineWidgetFrame(widget, cx, cy, rw, rh, mx, my,
																  topmost, leftmost, config, bounded)
			}

			if (widget.widgetType == 'Image' && 
				isDependentAttribute(widget, 'file'))  { 
			  view['#autoLayout'] = true
			} 

		  }

		  if (setWidgetFrame) { 
			//if (widget.nodeType != 'Image' || bounded) {  
			widget._frame = [cx, cy, w, h, wfill, hfill]
			info "[Layout] widget ${widget.nodeType} ${widget.id} _frame: ${widget._frame}" 

			if (constraints) { 
			  if (wfill) { 
				widget['#layout'] << constraints.right
			  }
			  if (hfill) { 
				widget['#layout'] << constraints.bottom
			  }
			}
		  }

		  vw = max(vw, cx + w)
		  vh = max(vh, cy + h)
	  
		  if (horizontal) { 
			if (w > 0) { 
			  cx += (w + config.defaults.gap)
			  rw -= (cx - x)
			  leftmost = false
			}
			if (constraints) { 
			  if (prev)
				widget['#layout'] << [ 'next', prev.id, config.defaults.gap ]
			}
		  } else { 
			if (h > 0) { 
			  cy += (h + config.defaults.gap)
			  rh -= (cy - y)
			  topmost = false
			}
			if (constraints) { 
			  if (prev) {
				if (Language.isGroup(prev.nodeType)) { 
				  widget['#layout'] << [ 'below', prev.children.id, config.defaults.gap ] 
				} else { 
				  widget['#layout'] << [ 'below', prev.id, config.defaults.gap ]
				}
			  }
			}
		  }
		  prev = widget
		}
		//topmost = false
		info "[Layout] widget ${widget.nodeType} ${widget.id} #layout: ${widget['#layout']}" 
      }
    }
    return [vw - x, vh - y]
  }

  static layoutTable(View view, 
					 int x, int y,     // current position
					 int rw, int rh,   // remaining width and height
					 int mx, int my,   // margin x and y 
					 def config,
					 def constraints,
					 boolean bounded = true, 
					 boolean setWidgetFrame = true) { 
    info "[LayoutTable] id=${view.id}"
    int vw = 0, vh = 0;
    if (view && view.children) { 
      //boolean horizontal = false // alwaws vertical 

      def colWidth = [:]  // the maximum width of each column 
      def colStretch = [:] // whether each column is stretchable 
      int cx = x, cy = y;
      int w = 0, h = 0;
      boolean wfill = false, hfill = false 
      boolean topmost = true, leftmost = true
	  Widget prev = null
	  Widget prevRow = null
      // the first pass 
      view.children.each { widget -> 
		info "[LayoutTable] row ${view.id} child ${widget.id} isUI=${Language.isUI(widget.nodeType)}"

		if (Language.isUI(widget.nodeType) &&
			!Language.isPopup(widget.nodeType)) { 
		  if (Language.isGroup(widget.nodeType)) {  
			int rw0 = rw - (cx - x)
			int rh0 = rh - (cy - y)
			(w, h) = layout(widget, cx, cy, rw0, rh0, mx, my, config, null, bounded, setWidgetFrame) // true 

			if (widget.nodeType == 'Row') { 
			  widget.children.eachWithIndex { w1, i ->
				colStretch[i] = w1._frame[4]  // wfill
				int width = w1._frame[2]
				int cwidth = colWidth[i] ?: 0
				colWidth[i] = max(width, cwidth)
			  }

			  prevRow = widget
			}
		  } else { 
			(cx, cy, w, h, wfill, hfill) = determineWidgetFrame(widget, cx, cy, rw, rh, mx, my,
																topmost, leftmost, config, bounded)
		  }

		  vw = max(vw, cx + w)
		  vh = max(vh, cy + h)
	  
		  // vertical 
		  cy += (h + config.defaults.gap)
		  rh -= (cy - y)

		  prev = widget
		}
		topmost = false
      }

      info "[LayoutTable] colWidth: ${colWidth}  colStretch: ${colStretch}"
      view._colWidth = colWidth
      view._colStretch = colStretch

      // the second pass 
      cx = x; cy = y;
      w = 0; h = 0;
      wfill = false; hfill = false; 
      topmost = true; leftmost = true;
	  prev = null
	  prevRow = null
      view.children.each { widget -> 
		info "[LayoutTable] row ${view.id} child ${widget.id} isUI=${Language.isUI(widget.nodeType)}"

		if (Language.isUI(widget.nodeType) &&
			!Language.isPopup(widget.nodeType)) { 
		  if (constraints) { 
			widget['#layout'] = []
			if (topmost) widget['#layout'] << constraints.top 
			if (leftmost) widget['#layout'] << constraints.left 
		  }
		  if (Language.isGroup(widget.nodeType)) {  

			if (constraints) { 
			  if (prev) { 
				if (Language.isGroup(prev.nodeType)) { 
				  constraints.top = [ 'below', prev.children.id, config.defaults.gap ]
				} else { 
				  constraints.top = [ 'below', prev.id, config.defaults.gap ]
				}
			  }
			}

			int rw0 = rw - (cx - x)
			int rh0 = rh - (cy - y)
			if (widget.nodeType == 'Row') { 
			  (w, h) = layoutTableRow(widget, cx, cy, rw0, rh0, mx, my, 
									  colWidth, colStretch,
									  config, constraints, bounded, setWidgetFrame)

			  if (constraints) { 
				widget.children.eachWithIndex { w1, i ->
				  if (prevRow) { 
					Widget w0 = prevRow[i]
					w1['#layout'] << [ 'right', w0.id, 0 ]
				  }
				}
			  }

			  prevRow = widget
			} else { 
			  (w, h) = layout(widget, cx, cy, rw0, rh0, mx, my, config, constraints, bounded, setWidgetFrame)
			}
		  } else { 
			if (widget.widgetType == 'Image' && 
				isDependentAttribute(widget, 'file'))  { 
			  view['#autoLayout'] = true
			} 

			if (widget.frame) { 
			  (cx, cy, w, h) = widget.frame
			} else { 
			  (cx, cy, w, h, wfill, hfill) = determineWidgetFrame(widget, cx, cy, rw, rh, mx, my,
																  topmost, leftmost, config, bounded)
			}
		  }

		  if (setWidgetFrame) { 
			//if (widget.nodeType != 'Image' || bounded) {  
			widget._frame = [cx, cy, w, h, wfill, hfill]
			info "[LayoutTable] widget ${widget.nodeType} ${widget.id} _frame: ${widget._frame}"

			if (constraints) { 
			  if (wfill) { 
				widget['#layout'] << constraints.right
			  }
			  if (hfill) { 
				widget['#layout'] << constraints.bottom
			  }
			} 
		  }

		  vw = max(vw, cx + w)
		  vh = max(vh, cy + h)
	  
		  // vertical 
		  cy += (h + config.defaults.gap)
		  rh -= (cy - y)

		  if (constraints) { 
			if (prev) {
			  if (Language.isGroup(prev.nodeType)) { 
				widget['#layout'] << [ 'below', prev.children.id, config.defaults.gap ] 
			  } else { 
				widget['#layout'] << [ 'below', prev.id, config.defaults.gap ]
			  }
			}
		  }

		  prev = widget
		}
		topmost = false
		info "[LayoutTable] widget ${widget.nodeType} ${widget.id} #layout: ${widget['#layout']}" 
      }

    }
    return [vw - x, vh - y]
  }

  // return [width, heght]
  static layoutTableRow(View view, 
						int x, int y,     // current position
						int rw, int rh,   // remaining width and height
						int mx, int my,   // margin x and y 
						def colWidth,     // col width, Map: i -> int
						def colStretch,   // col stretchable, Map: i -> boolean
						def config,
						def constraints,
						boolean bounded = true, 
						boolean setWidgetFrame = true) { 
    info "[LayoutTableRow] id=${view.id}"

    int vw = 0, vh = 0;
    if (view && view.children) { 
      // boolean horizontal = true 

      int cx = x, cy = y
      int w = 0, h = 0
      boolean wfill = false, hfill = false 
      boolean topmost = true, leftmost = true
	  Widget prev = null
      view.children.eachWithIndex { widget, i -> 
		info "[LayoutTableRow] ${view.id} child ${widget.id} isUI=${Language.isUI(widget.nodeType)}"

		if (Language.isUI(widget.nodeType) &&
			!Language.isPopup(widget.nodeType)) { 
		  if (constraints) { 
			widget['#layout'] = []
			if (topmost) widget['#layout'] << constraints.top 
			if (leftmost) widget['#layout'] << constraints.left 
		  }

		  if (Language.isGroup(widget.nodeType)) {  
			if (prev) constraints.left = [ 'next', prev.id, config.defaults.gap ] 

			int rw0 = rw - (cx - x)
			int rh0 = rh - (cy - y)
			(w, h) = layout(widget, cx, cy, rw0, rh0, mx, my, config, constraints, bounded, setWidgetFrame)	    
		  } else { 
			if (widget.frame) { 
			  (cx, cy, w, h) = widget.frame
			} else { 
			  (cx, cy, w, h, wfill, hfill) = determineWidgetFrame(widget, cx, cy, rw, rh, mx, my,
																  topmost, leftmost, config, bounded)
			}
		  }
		  if (w < colWidth[i]) { // handle column width 
			w = colWidth[i]
			if (w > rw)  
			  w = rw
		  }

		  if (widget.nodeType == 'Image' && 
			  isDependentAttribute(widget, 'file'))  { 
			view['#autoLayout'] = true
		  } 

		  if (setWidgetFrame) { 
			if (widget.nodeType != 'Image' ||  
				rw > 0 || rh > 0) { 
			  widget._frame = [cx, cy, w, h, wfill, hfill]
			  info "[LayoutTableRow] widget ${widget.nodeType} ${widget.id} layout: ${widget._frame}" 

			  if (constraints) { 
				if (wfill) { 
				  widget['#layout'] << constraints.right
				}
				if (hfill) { 
				  widget['#layout'] << constraints.bottom
				}
			  }

			}
		  }

		  vw = max(vw, cx + w)
		  vh = max(vh, cy + h)
	  
		  // horizontal 
		  cx += (w + config.defaults.gap)
		  rw -= (cx - x)
		  leftmost = false

		  if (constraints) { 
			if (prev)
			  widget['#layout'] << [ 'next', prev.id, config.defaults.gap ]
		  }
		  prev = widget
		}
		//topmost = false
		info "[LayoutTableRow] widget ${widget.nodeType} ${widget.id} #layout: ${widget['#layout']}" 
      }
    }
    return [vw - x, vh - y]
  }

  //
  // utilities 
  //

  // return [x, y, w, h, wfill, hfill]
  static determineWidgetFrame(Widget widget,
							  int curX, int curY,           // current top-right position   
							  int maxWidth, int maxHeight,  // max width and height of the container 
							  int marginX, int marginY,     // margin x and y 
							  boolean topmost,
							  boolean leftmost,
							  def config,
							  boolean bounded = true) { 
	info "[Layout] determineWidgetFrame(): widget=[${widget.widgetType}, ${widget.id}] cur=(${curX},${curY}) max=(${maxWidth},${maxHeight})"

    int w = 0
    int h = 0
    boolean wfill = false, hfill = false  // stretchable width | height
    if (widget.size) { 
      (w, h) = widget.size 
    } else { 
      if (widget.width) { 
		if (widget.width == '*') { 
		  w = maxWidth
		  wfill = true
		} else {  
		  w = widget.width
		}
      }
      if (widget.height) { 
		if (widget.height == '*') { 
		  h = maxHeight
		  hfill = true
		} else {  
		  h = widget.height
		}
      }
    }


	String wname = getPlatformWidgetName(widget) ?: widget.widgetType 	
    boolean useTextWidth = config.defaults[wname].useTextWidth 
    int widthOffset = config.defaults[wname].widthOffset ?: 0
    if (useTextWidth && (widget.text != null || widget.image == null)) { 
	  int numLines = widget.lines ?: linesInString(widget.text as String)
      h = config.defaults[wname].height
	  String fname = config.defaults[wname].font.name
	  int fsize = config.defaults[wname].font.size
	  String fstyle = config.defaults[wname].font.style
	  def font = widget.font
	  if (font == null) font = widget.titleFont 
	  if (font instanceof Font) { 
		if (font.family) fname = font.family
		if (font.size > 0) fsize = font.size
		if (font.bold) { 
		  fstyle = 'bold'
		} else if (font.italic) { 
		  fstyle = 'italic'
		}
		h = max(h, getFontHeight(fname, fsize) * numLines + 6)
	  } else { 
		if (numLines > 1) { 
		  h = max(h, getFontHeight(fname, fsize) * numLines + 6)
		}
	  }
      if (w <= 0) { 
		if (widget.text) { 
		  w = getStringWidth(widget.text as String, fname, fsize, fstyle) + 20 + widthOffset
		} else { 
		  w = maxWidth
		  wfill = true
		}
	  }
    } 

	if (allowImage(widget)) { 
	  def (iw, ih) = getImageSizeForWidget(widget)
	  if (iw <= maxWidth && ih <= maxHeight ||
		  !bounded) { 
		if (w <= 0) w = iw
		if (h <= 0) h = ih
      }
    } 

    if (h <= 0) { 
      def v = config.defaults[wname].height
      info "[Layout] config.defaults[${wname}].height: ${v}"
      if (v instanceof Integer) { 
		if (v < 0) { 
		  h = maxHeight
		  hfill = true
		} else {  
		  h = v
		}
      } else { 
		if (v == '*') { 
		  h = maxHeight
		  hfill = true
		} else if (v == '=') { 
		  if (topmost) { 
			curY -= marginY
			h = maxHeight + 2 * marginY
		  } else { 
			h = maxHeight + marginY
		  }
		  hfill = true
		}
      }
    }
    if (w <= 0) { 
      def v = config.defaults[wname].width
      info "[Layout] config.defaults[${wname}].width: ${v}"
      if (v instanceof Integer) { 
		if (v < 0) { 
		  w = maxWidth
		  wfill = true
		} else {  
		  w = v
		}
      } else { 
		if (v == '*' ||
			v instanceof Integer && v < 0) { 
		  w = maxWidth
		  wfill = true
		} else if (v == '=') { 
		  if (leftmost) { 
			curX -= marginX
			w = maxWidth + 2 * marginX
		  } else { 
			w = maxWidth + marginX
		  }
		  wfill = true
		}
      }      
    }

    return [curX, curY, w, h, wfill, hfill]
  }


  static int getStringWidth(String text, String fname, int size, String style = null) { 
	(int) Math.ceil(FontMetrics.getStringWidth(text, fname, size, style))
  }

  static int getFontHeight(String fname, int size) { 
	(int) Math.ceil(FontMetrics.getFontHeight(fname, size))
  }

}