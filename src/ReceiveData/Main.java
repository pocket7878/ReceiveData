// vim: set foldmethod=marker :
package ReceiveData;

import com.sun.spot.io.j2me.radiogram.*;
import javax.microedition.io.*;

import javax.swing.*;
import java.awt.*;
import org.jfree.chart.*;//{{{
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.*;
import org.jfree.data.time.*;//}}}
import java.util.Date; //{{{
import java.util.TimeZone;
import java.util.Calendar;//}}}
import java.text.*;
import java.awt.Font; 
import java.io.*;

public class Main extends JFrame implements Runnable{
	static final int HOST_PORT = 67;
	private Thread th;
	private TimeSeries AccelSeries;//{{{
	private TimeSeries InsideTempSeries;
	private TimeSeries OutsideTempSeries;
	private TimeSeries LM60TempSeries;
	private TimeSeries BrightSeries;
	private TimeSeries WetSeries;
	private TimeSeries UnconfortSeries;
	private TimeSeries PressureSeries;//}}}
	private long now = 0L;
	private JPanel panel;
	private PrintWriter pw;

	private Calendar cal;//{{{
	private int year;
	private int month;
	private int date;
	private int hour;
	private int minutes;
	private int second;
	private int millisecond;//}}}


	public Main() {//{{{
		panel = new JPanel();
//		panel.setLayout(new BoxLayout( panel, BoxLayout.Y_AXIS));
		panel.setLayout(new GridLayout(2,2));
		getContentPane().add(panel);
		initChart();
		th = new Thread(this);
		th.start();
	}//}}}

	public void initChart() {//{{{
		//Define Timeseries
		AccelSeries = new TimeSeries("Accel", Millisecond.class);
		InsideTempSeries = new TimeSeries("Inside Temp", Millisecond.class);
		OutsideTempSeries = new TimeSeries("Outside Temp", Millisecond.class);
		LM60TempSeries = new TimeSeries("LM60 Temp", Millisecond.class);
		BrightSeries = new TimeSeries("Bright", Millisecond.class);
		PressureSeries = new TimeSeries("Pressure", Millisecond.class);
		WetSeries = new TimeSeries("Shitudo", Millisecond.class);
		UnconfortSeries = new TimeSeries("Unconfort", Millisecond.class);

		//Define dataSet
		TimeSeriesCollection accelset = new TimeSeriesCollection();
		TimeSeriesCollection tempset = new TimeSeriesCollection();
		TimeSeriesCollection brightset = new TimeSeriesCollection();
		TimeSeriesCollection pressset = new TimeSeriesCollection();
		TimeSeriesCollection wetset = new TimeSeriesCollection();
		TimeSeriesCollection unconfortset = new TimeSeriesCollection();


		accelset.addSeries(AccelSeries);
		tempset.addSeries(InsideTempSeries);
		tempset.addSeries(OutsideTempSeries);
		tempset.addSeries(LM60TempSeries);
		brightset.addSeries(BrightSeries);
		

		pressset.addSeries(PressureSeries);
		wetset.addSeries(WetSeries);
		unconfortset.addSeries(UnconfortSeries);

		//Define Axis
		DateAxis accelDomainAxis = new DateAxis("Time");
		accelDomainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
		ValueAxis accelRangeAxis = new NumberAxis("Accelaration");

		DateAxis tempDomainAxis = new DateAxis("Time");
		tempDomainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
		ValueAxis tempRangeAxis = new NumberAxis("Temperature");

		DateAxis brightDomainAxis = new DateAxis("Time");
		brightDomainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
		ValueAxis brightRangeAxis = new NumberAxis("Brightness");

		DateAxis pressDomainAxis = new DateAxis("Time");
		pressDomainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
		ValueAxis pressRangeAxis = new NumberAxis("Pressure");

		DateAxis wetDomainAxis = new DateAxis("Time");
		wetDomainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
		ValueAxis wetRangeAxis = new NumberAxis("Shitudo");

		DateAxis unconfortDomainAxis = new DateAxis("Time");
		unconfortDomainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
		ValueAxis unconfortRangeAxis = new NumberAxis("Unconfort");

		//Define XY Renderer
		XYItemRenderer accelrenderer = new StandardXYItemRenderer();
		accelrenderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		XYItemRenderer temprenderer = new StandardXYItemRenderer();
		temprenderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
		XYItemRenderer brightrenderer = new StandardXYItemRenderer();
		brightrenderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
		XYItemRenderer pressrenderer = new StandardXYItemRenderer();
		pressrenderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
		XYItemRenderer wetrenderer = new StandardXYItemRenderer();
		wetrenderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
		XYItemRenderer unconfortrenderer = new StandardXYItemRenderer();
		unconfortrenderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		//Define Plot
		XYPlot accelPlot = new XYPlot(accelset,accelDomainAxis,accelRangeAxis,accelrenderer);
		accelPlot.getRenderer().setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		XYPlot tempPlot = new XYPlot(tempset,tempDomainAxis, tempRangeAxis, temprenderer);
		tempPlot.getRenderer().setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		XYPlot brightPlot = new XYPlot(brightset, brightDomainAxis, brightRangeAxis, brightrenderer);
		brightPlot.getRenderer().setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		XYPlot pressPlot = new XYPlot(pressset, pressDomainAxis, pressRangeAxis, pressrenderer);
		pressPlot.getRenderer().setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		XYPlot wetPlot = new XYPlot(wetset, wetDomainAxis, wetRangeAxis, wetrenderer);
		wetPlot.getRenderer().setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		XYPlot unconfortPlot = new XYPlot(unconfortset, unconfortDomainAxis, unconfortRangeAxis, unconfortrenderer);
		unconfortPlot.getRenderer().setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		JFreeChart accelChart = new JFreeChart("XYZ Acceleration", JFreeChart.DEFAULT_TITLE_FONT, accelPlot, true);
		LegendTitle accelLegend = accelChart.getLegend();
		accelLegend.setItemFont(new Font("Ariel", Font.BOLD, 20));
		ChartPanel accelPanel = new ChartPanel(accelChart);


		JFreeChart TempChart = new JFreeChart("Temperature", JFreeChart.DEFAULT_TITLE_FONT, tempPlot, true);
		LegendTitle TempLegend = TempChart.getLegend();
		TempLegend.setItemFont(new Font("Ariel", Font.BOLD, 20));
		ChartPanel TempPanel = new ChartPanel(TempChart);

		JFreeChart BrightChart = new JFreeChart("Brightness", JFreeChart.DEFAULT_TITLE_FONT, brightPlot, true);
		LegendTitle BrightLegend = BrightChart.getLegend();
		BrightLegend.setItemFont(new Font("Ariel", Font.BOLD, 20));
		ChartPanel BrightPanel = new ChartPanel(BrightChart);

		JFreeChart PressChart = new JFreeChart("Pressure", JFreeChart.DEFAULT_TITLE_FONT, pressPlot, true);
		LegendTitle PressLegend = PressChart.getLegend();
		PressLegend.setItemFont(new Font("Ariel", Font.BOLD, 20));
		ChartPanel PressPanel = new ChartPanel(PressChart);

		JFreeChart WetChart = new JFreeChart("Shitudo", JFreeChart.DEFAULT_TITLE_FONT, wetPlot, true);
		LegendTitle WetLegend = WetChart.getLegend();
		WetLegend.setItemFont(new Font("Ariel", Font.BOLD, 20));
		ChartPanel WetPanel = new ChartPanel(WetChart);

		JFreeChart UnconfortChart = new JFreeChart("Unconfort", JFreeChart.DEFAULT_TITLE_FONT, unconfortPlot, true);
		LegendTitle UnconfortLegend = UnconfortChart.getLegend();
		UnconfortLegend.setItemFont(new Font("Ariel", Font.BOLD, 20));
		ChartPanel UnconfortPanel = new ChartPanel(UnconfortChart);

		panel.add(accelPanel);
		panel.add(TempPanel);
		panel.add(BrightPanel);
		panel.add(PressPanel);
		panel.add(WetPanel);
		panel.add(UnconfortPanel);
	}//}}}

	public void run() {//{{{
		RadiogramConnection rCon = null;
		Datagram dg = null;
		double accel, insideTemp, outsideTemp, lm60, bright;
		//add-hook
		double press;
		double wet;
		double unconfort;

		try {
			rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
			dg = rCon.newDatagram(rCon.getMaximumLength());
		} catch (Exception e) {
			System.err.println("setUp caught " + e.getMessage());
		}
		//Get Calender Data
		//getContentPane().add(panel);
		cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH) + 1;
		date = cal.get(Calendar.DAY_OF_MONTH);
		hour = cal.get(Calendar.HOUR);
		minutes = cal.get(Calendar.MINUTE);
		second = cal.get(Calendar.SECOND);
		millisecond = cal.get(Calendar.MILLISECOND);
		try {
		//Make Printwriter
			pw = new PrintWriter( new FileWriter ("./data/Data" + 
						Integer.toString(year) + "_" + 
						Integer.toString(month) + "_" +
						Integer.toString(date) + "_" + 
						Integer.toString(hour) + "_" +
						Integer.toString(minutes) + 
						".data"));
		} catch(IOException p) {
			System.err.println("Can't make printwriter");
			System.exit(1);
		}

		TimeZone jst = new TimeZone() {//{{{
			public int getOffset(int era, int year, int month, int day,
					int dayOfWeek, int millis) {
				return 9 * 3600 * 1000;
			}
			public int getRawOffset() {
				return 9 * 3600 * 1000;
			}
			public boolean useDaylightTime() {
				return false;
			}
			public boolean inDaylightTime(Date data) {
				return false;
			}
			public void setRawOffset(int offset) {
			}
		};//}}}

		//Main Loop
		while (true) {
			try {
				rCon.receive(dg);
				now = dg.readLong();
//				x = dg.readDouble();
//				y = dg.readDouble();
//				z = dg.readDouble();
				accel = dg.readDouble();
				bright = dg.readDouble();
				insideTemp = dg.readDouble();
				outsideTemp = dg.readDouble();
				lm60 = dg.readDouble();
				press = dg.readDouble();
				wet = dg.readDouble();
				unconfort = dg.readDouble();

				// Calender
				cal = Calendar.getInstance(jst);
				cal.setTime(new Date(now));
				year = cal.get(Calendar.YEAR);
				month = cal.get(Calendar.MONTH) + 1;
				date = cal.get(Calendar.DAY_OF_MONTH);
				hour = cal.get(Calendar.HOUR_OF_DAY);
				minutes = cal.get(Calendar.MINUTE);
				second = cal.get(Calendar.SECOND);
				millisecond = cal.get(Calendar.MILLISECOND);

				//Add new Data
//				XSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), x);
//				YSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), y);
//				ZSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), z);
				AccelSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), accel);
				InsideTempSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), insideTemp);
				OutsideTempSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), outsideTemp);
				LM60TempSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), lm60);
				BrightSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), bright); 
				PressureSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), press); 
				WetSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), wet); 
				UnconfortSeries.add(new Millisecond(millisecond, second, minutes, hour, date, month, year), unconfort);


				pw.printf("%04d%02d%02d,%02d:%02d:%02d.%04d,%f,%f,%f,%f,%f,%f,%f,%f\n",
						year,month,date,
						hour,minutes,second,millisecond,
						accel,
						insideTemp,
						outsideTemp,
						lm60,
						bright,
						press,
						wet,
						unconfort);
				pw.flush();
			} catch(Exception e) {
				System.err.println("Caught " + e + " while reading sensor samples.");
				try {
					rCon.close();
				} catch(IOException ignore) {
				}
			} 
		} 
	}//}}}

	public static void main(String[] args) {//{{{
		Main main = new Main();
		main.setSize(600,800);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
	}//}}}

}
