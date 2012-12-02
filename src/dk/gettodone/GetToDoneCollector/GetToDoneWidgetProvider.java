package dk.gettodone.GetToDoneCollector;
import dk.gettodone.GetToDoneCollector.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class GetToDoneWidgetProvider extends AppWidgetProvider {
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
	    for (int i = 0; i < N; i++) {
	        int appWidgetId = appWidgetIds[i];
	        Intent intent = new Intent(context, GetToDoneActivity.class);
	        intent.putExtra("EXTRA_SPEECH_TO_TEXT", false);
	        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
	        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget1);
	        views.setOnClickPendingIntent(R.id.button1, pendingIntent);

	        intent = new Intent(context, GetToDoneActivity.class);
	        intent.putExtra("EXTRA_SPEECH_TO_TEXT", true);
	        pendingIntent = PendingIntent.getActivity(context, 1, intent, 0);	        
	        views.setOnClickPendingIntent(R.id.button2, pendingIntent);
	        appWidgetManager.updateAppWidget(appWidgetId, views);
	    }
	}
}
