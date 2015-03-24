package chart;

import java.util.Map;
import java.util.HashMap;

import repast.simphony.statecharts.*;
import repast.simphony.statecharts.generator.GeneratedFor;

import signalGreen.src.*;

@GeneratedFor("_ggYjwNJOEeSytrT9Z7eDDw")
public class Statechart extends DefaultStateChart<signalGreen.src.signalGreen> {

	public static Statechart createStateChart(
			signalGreen.src.signalGreen agent, double begin) {
		Statechart result = createStateChart(agent);
		StateChartScheduler.INSTANCE.scheduleBeginTime(begin, result);
		return result;
	}

	public static Statechart createStateChart(signalGreen.src.signalGreen agent) {
		StatechartGenerator generator = new StatechartGenerator();
		return generator.build(agent);
	}

	private Statechart(signalGreen.src.signalGreen agent) {
		super(agent);
	}

	private static class MyStateChartBuilder extends
			StateChartBuilder<signalGreen.src.signalGreen> {

		public MyStateChartBuilder(signalGreen.src.signalGreen agent,
				AbstractState<signalGreen.src.signalGreen> entryState,
				String entryStateUuid) {
			super(agent, entryState, entryStateUuid);
			setPriority(0.0);
		}

		@Override
		public Statechart build() {
			Statechart result = new Statechart(getAgent());
			setStateChartProperties(result);
			return result;
		}
	}

	private static class StatechartGenerator {

		private Map<String, AbstractState<signalGreen>> stateMap = new HashMap<String, AbstractState<signalGreen>>();

		public Statechart build(signalGreen agent) {
			throw new UnsupportedOperationException(
					"Statechart has not been defined.");

		}

		private void createTransitions(MyStateChartBuilder mscb) {

		}

	}
}
