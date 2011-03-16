package inat.graph;

import java.awt.*;
import java.awt.geom.Point2D;

public class Series {
	protected static int seriesCounter = 0;
	
	private P[] data = null;
	private String name = "";
	private boolean enabled = true;
	private Scale scale = null;
	private Series master = null, slave = null; //ideally, the slave series should be used to represent confidence intervals for the corresponding master series
	public static String SLAVE_SUFFIX = "_stddev"; //for a series to be a representation of confidence intervals of series ABC, its name should be "ABC" + SLAVE_SUFFIX (suffix can have any capitalization)
	private Color myColor = null;
	private boolean changeColor = false;
	
	public Series(P[] data) {
		this(data, new Scale());
	}
	
	public Series(P[] data, Scale scale) {
		this(data, scale, "Series " + (++seriesCounter)); 
	}
	
	public Series(P[] data, Scale scale, String name) {
		this.data = data;
		this.setScale(scale);
		this.name = name;
	}
	
	public void setScale(Scale scale) {
		this.scale = scale;
		if (!isSlave()) {
			this.scale.addData(data);
		} else {
			P[] dataLow = new P[data.length];
			P[] dataHigh = new P[data.length];
			for (int i=0;i<data.length;i++) {
				dataLow[i] = new P(data[i].x, master.data[i].y - data[i].y);
				dataHigh[i] = new P(data[i].x, master.data[i].y + data[i].y);
			}
			this.scale.addData(dataLow);
			this.scale.addData(dataHigh);
		}
	}
	
	public Scale getScale() {
		return this.scale;
	}
	
	public String getName() {
		return this.name;
	}
	
	public P[] getData() {
		return this.data;
	}
	
	public void setSlave(Series s) {
		this.setSlave(s, true);
	}
	
	private void setSlave(Series s, boolean propagate) {
		this.slave = s;
		if (propagate) {
			s.setMaster(this, false);
		}
	}
	
	public void setMaster(Series s) {
		this.setMaster(s, true);
	}
	
	private void setMaster(Series s, boolean propagate) {
		this.master = s;
		this.setScale(this.master.getScale());
		if (propagate) {
			s.setSlave(this, false);
		}
	}
	
	public boolean isSlave() {
		return this.master != null;
	}
	
	public boolean isMaster() {
		return this.slave != null; 
	}
	
	//wether to show this series or not
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (slave != null) {
			this.slave.setEnabled(enabled);
		}
	}
	
	public boolean getEnabled() {
		return this.enabled;
	}
	
	public void setChangeColor(boolean changeColor) {
		this.changeColor = changeColor;
	}
	
	public boolean getChangeColor() {
		return this.changeColor;
	}
	
	public Color getColor() {
		return this.myColor;
	}
	
	public void plot(Graphics2D g, Rectangle bounds) {
		scale.computeScale(bounds);
		double scaleX = scale.getXScale(),
			   scaleY = scale.getYScale(),
			   minX = scale.getMinX(),
			   minY = scale.getMinY();
		if (!enabled) return;
		
		if (master != null) {
			myColor = master.myColor;
			P[] masterData = master.getData();
			P vecchio = null;
			int i = 0;
			Color c = g.getColor();
			for (P punto : data) {
				for (;i<masterData.length && masterData[i].x<punto.x;i++);
				if (i < masterData.length) {
					if (i > 0) {
						float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
						Color c1 = Color.getHSBColor(hsb[0], hsb[1]/3, hsb[2]);
							  //c2 = Color.getHSBColor(hsb[0], hsb[1]*3/2, hsb[2]);
						float[] rgb = c1.getRGBComponents(null);
						Color c3 = new Color(rgb[0], rgb[1], rgb[2], 0.5f);
						rgb = c.getRGBColorComponents(null);
						Color c4 = new Color(rgb[0], rgb[1], rgb[2], 0.6f);
						/*g.setColor(Color.getHSBColor(hsb[0], hsb[1]/4, hsb[2]));
						g.drawLine((int)(bounds.x + scaleX * (masterData[i-1].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - vecchio.y - minY)),
								   (int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y - punto.y - minY)));
						g.drawLine((int)(bounds.x + scaleX * (masterData[i-1].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y + vecchio.y - minY)),
								   (int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y + punto.y - minY)));*/
						/*Polygon grayedError = new Polygon();
						grayedError.addPoint((int)(bounds.x + scaleX * (masterData[i-1].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - vecchio.y - minY)));
						grayedError.addPoint((int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y - punto.y - minY)));
						grayedError.addPoint((int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y + punto.y - minY)));
						grayedError.addPoint((int)(bounds.x + scaleX * (masterData[i-1].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y + vecchio.y - minY)));
						g.setPaint(new GradientPaint(new Point2D.Float((int)(bounds.x + scaleX * (masterData[i-1].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - vecchio.y - minY))), c3, new Point2D.Float((int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y + punto.y - minY))), c4));
						g.fill(grayedError);*/
						/*Polygon error1 = new Polygon();
						error1.addPoint((int)(bounds.x + scaleX * (masterData[i-1].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - minY)));
						error1.addPoint((int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y - minY)));
						error1.addPoint((int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y - punto.y - minY)));
						error1.addPoint((int)(bounds.x + scaleX * (masterData[i-1].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - vecchio.y - minY)));
						g.drawLine((int)(bounds.x + scaleX * (masterData[i-1].x + masterData[i].x) / 2.0 - minX), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y + masterData[i].y) / 2.0 - minY), (int)(bounds.x + scaleX * (masterData[i-1].x + masterData[i].x) / 2.0 - minX), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - vecchio.y + masterData[i].y - punto.y) / 2.0 - minY));
						g.setPaint(new GradientPaint((float)(bounds.x + scaleX * (masterData[i-1].x + masterData[i].x) / 2.0 - minX), (float)(bounds.y + bounds.height - scaleY * (masterData[i-1].y + masterData[i].y) / 2.0 - minY), c, (float)(bounds.x + scaleX * (masterData[i-1].x + masterData[i].x) / 2.0 - minX), (float)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - vecchio.y + masterData[i].y - punto.y) / 2.0 - minY), c4));
						g.fill(error1);*/
						
						//I would like to make it simpler, so that it does not negatively influence performances, but I have no time..
						double maxY = Math.max(vecchio.y, punto.y);
						Point2D.Float A = new Point2D.Float((float)(bounds.x + scaleX * (masterData[i-1].x - minX)), (float)(bounds.y + bounds.height - scaleY * (masterData[i-1].y + maxY - minY))),
									  B = new Point2D.Float((float)(bounds.x + scaleX * (masterData[i].x - minX)), (float)(bounds.y + bounds.height - scaleY * (masterData[i].y + maxY - minY))),
									  C = new Point2D.Float((float)(bounds.x + scaleX * (masterData[i].x - minX)), (float)(bounds.y + bounds.height - scaleY * (masterData[i].y - maxY - minY))),
									  D = new Point2D.Float((float)(bounds.x + scaleX * (masterData[i-1].x - minX)), (float)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - maxY - minY))),
									  E = new Point2D.Float((float)(bounds.x + scaleX * (masterData[i-1].x - minX)), (float)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - minY))),
									  F = new Point2D.Float((float)(bounds.x + scaleX * (masterData[i].x - minX)), (float)(bounds.y + bounds.height - scaleY * (masterData[i].y - minY))),
									  I = new Point2D.Float((E.x + F.x) / 2.0f, (E.y + F.y) / 2.0f);
						float Gx = (A.x*(B.y-A.y)/(B.x-A.x) - A.y + I.x*(B.x-A.x)/(B.y-A.y) + I.y) / ((B.y-A.y)/(B.x-A.x) + (B.x-A.x)/(B.y-A.y)),
							  Gy = (B.y-A.y)/(B.x-A.x) * (Gx-A.x) + A.y;
						Point2D.Float G = new Point2D.Float(Gx, Gy);
						float Hx = (D.x*(C.y-D.y)/(C.x-D.x) - D.y + I.x*(C.x-D.x)/(C.y-D.y) + I.y) / ((C.y-D.y)/(C.x-D.x) + (C.x-D.x)/(C.y-D.y)),
							  Hy = (C.y-D.y)/(C.x-D.x) * (Gx-D.x) + D.y;
						Point2D.Float H = new Point2D.Float(Hx, Hy);
						Polygon error1 = new Polygon();
						error1.addPoint((int)E.x, (int)E.y);
						error1.addPoint((int)F.x, (int)F.y);
						error1.addPoint((int)B.x, (int)(bounds.y + bounds.height - scaleY * (masterData[i].y + punto.y - minY)));
						error1.addPoint((int)A.x, (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y + vecchio.y - minY)));
						//g.drawLine((int)I.x, (int)I.y, (int)G.x, (int)G.y);
						//g.drawLine((int)A.x, (int)A.y, (int)B.x, (int)B.y);
						g.setPaint(new GradientPaint(I, c4, G, c3));
						g.fill(error1);
						Polygon error2 = new Polygon();
						error2.addPoint((int)E.x, (int)E.y);
						error2.addPoint((int)F.x, (int)F.y);
						error2.addPoint((int)C.x, (int)(bounds.y + bounds.height - scaleY * (masterData[i].y - punto.y - minY)));
						error2.addPoint((int)D.x, (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - vecchio.y - minY)));
						g.setPaint(new GradientPaint(I, c4, H, c3));
						g.fill(error2);
					}
				}
				vecchio = punto;
			}
			g.setColor(c);
			i = 0;
			for (P punto : data) {
				for (;i<masterData.length && masterData[i].x<punto.x;i++);
				if (i < masterData.length) {
					g.drawLine((int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y - punto.y - minY)), 
							(int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y  + punto.y - minY)));
					g.drawLine((int)(bounds.x + scaleX * (masterData[i].x - minX)) - 3, (int)(bounds.y + bounds.height - scaleY * (masterData[i].y - punto.y - minY)), 
							(int)(bounds.x + scaleX * (masterData[i].x - minX)) + 3, (int)(bounds.y + bounds.height - scaleY * (masterData[i].y  - punto.y - minY)));
					g.drawLine((int)(bounds.x + scaleX * (masterData[i].x - minX)) - 3, (int)(bounds.y + bounds.height - scaleY * (masterData[i].y + punto.y - minY)), 
							(int)(bounds.x + scaleX * (masterData[i].x - minX)) + 3, (int)(bounds.y + bounds.height - scaleY * (masterData[i].y  + punto.y - minY)));
					if (i > 0) {
						g.drawLine((int)(bounds.x + scaleX * (masterData[i-1].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i-1].y - minY)),
								   (int)(bounds.x + scaleX * (masterData[i].x - minX)), (int)(bounds.y + bounds.height - scaleY * (masterData[i].y - minY)));
					}
				}
				vecchio = punto;
			}
		} else {
			myColor = g.getColor();
			if (slave != null) {
				slave.myColor = myColor;
			}
			P vecchio = data[0];
			for (int j=1;j<data.length;j++) {
				P punto = data[j];
				g.drawLine((int)(bounds.x + scaleX * (vecchio.x - minX)), (int)(bounds.y + bounds.height - scaleY * (vecchio.y - minY)), 
						(int)(bounds.x + scaleX * (punto.x - minX)), (int)(bounds.y + bounds.height - scaleY * (punto.y - minY)));
				/*g.drawString("" + punto.x + ", " + punto.y, (int)(bounds.x + scaleX * (punto.x - minX)), (int)(bounds.y + bounds.height - scaleY * (punto.y - minY)));
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine((int)(bounds.x + scaleX * (punto.x - minX)), (int)(bounds.y + bounds.height - scaleY * (punto.y - minY)), (int)(bounds.x + scaleX * (punto.x - minX)), bounds.height + bounds.y);
				g.drawLine((int)(bounds.x + scaleX * (punto.x - minX)), (int)(bounds.y + bounds.height - scaleY * (punto.y - minY)), bounds.x, (int)(bounds.y + bounds.height - scaleY * (punto.y - minY)));
				g.setColor(myColor);*/
				vecchio = punto;
			}
		}
	}
}