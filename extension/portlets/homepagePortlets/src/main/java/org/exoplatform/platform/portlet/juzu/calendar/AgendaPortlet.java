package org.exoplatform.platform.portlet.juzu.calendar;

import juzu.*;
import juzu.plugin.ajax.Ajax;
import org.exoplatform.calendar.service.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

/**
 * @author <a href="fbradai@exoplatform.com">Fbradai</a>
 * @date 13/12/12
 */
@SessionScoped
public class AgendaPortlet extends Controller {
    static Map<String, org.exoplatform.calendar.service.Calendar> calendarDisplayedMap = new HashMap<String, org.exoplatform.calendar.service.Calendar>();
    static Map<String, org.exoplatform.calendar.service.Calendar> calendarNonDisplayedMap = new HashMap<String, org.exoplatform.calendar.service.Calendar>();

    static Set<org.exoplatform.calendar.service.Calendar> calendarDisplayedList = new HashSet<org.exoplatform.calendar.service.Calendar>();
    static Set<org.exoplatform.calendar.service.Calendar> calendarNonDisplayedList = new HashSet<org.exoplatform.calendar.service.Calendar>();
    static Set<CalendarEvent> eventsDisplayedList = new HashSet<CalendarEvent>();
    static Set<CalendarEvent> tasksDisplayedList = new HashSet<CalendarEvent>();
    static Set<org.exoplatform.calendar.service.Calendar> searchResult = new HashSet<org.exoplatform.calendar.service.Calendar>();
    static String nbclick = "0";

    CalendarService calendarService_;
    OrganizationService organization_;
    private static Log log = ExoLogger.getLogger(AgendaPortlet.class);


    @Inject
    public AgendaPortlet(CalendarService calendarService, OrganizationService organization) {
        calendarService_ = calendarService;
        organization_ = organization;
    }


    @Inject
    @Path("calendar.gtmpl")
    org.exoplatform.platform.portlet.juzu.calendar.templates.calendar calendar;

    @Inject
    @Path("settings.gtmpl")
    org.exoplatform.platform.portlet.juzu.calendar.templates.settings setting;

    @Inject
    @Path("search.gtmpl")
    org.exoplatform.platform.portlet.juzu.calendar.templates.search search;

    @View
    public void index() throws Exception {

        eventsDisplayedList.clear();
        calendarDisplayedMap.clear();
        calendarDisplayedList.clear();
        tasksDisplayedList.clear();

        String username = renderContext.getSecurityContext().getRemoteUser();
        Long date = new Date().getTime();
        int int_nb_click = Integer.parseInt(nbclick);
        if (int_nb_click != 0) date = incDecJour(date, int_nb_click);

        SimpleDateFormat sdf = null;
        CalendarSetting set = getCalendarSetting(username);
        if (set != null) sdf = new SimpleDateFormat(set.getDateFormat());
        else sdf = new SimpleDateFormat("MM/dd/yy");
        String date_act = sdf.format(new Date(date));

        Date comp = sdf.parse(date_act);

        List<CalendarEvent> userEvents = getEvents(username);

        if ((userEvents != null) && (!userEvents.isEmpty())) {
            Iterator itr = userEvents.iterator();
            while (itr.hasNext()) {

                CalendarEvent event = (CalendarEvent) itr.next();
                Date from = sdf.parse(sdf.format(event.getFromDateTime()));
                Date to = sdf.parse(sdf.format(event.getToDateTime()));

                if ((from.compareTo(comp) <= 0) && (to.compareTo(comp) >= 0)) {

                    if (event.getEventType().equals(CalendarEvent.TYPE_EVENT)) eventsDisplayedList.add(event);
                    else tasksDisplayedList.add(event);

                    if (!(calendarDisplayedMap.containsKey(event.getCalendarId()))) {
                        org.exoplatform.calendar.service.Calendar calendar = calendarService_.getUserCalendar(username, event.getCalendarId());
                        if (calendar == null) calendar = calendarService_.getGroupCalendar(event.getCalendarId());
                        calendarDisplayedMap.put(calendar.getId(), calendar);
                        calendarDisplayedList.add(calendar);
                    }
                }
            }
        }
        Iterator itr1 = getAllCal(username).iterator();
        while (itr1.hasNext()) {
            org.exoplatform.calendar.service.Calendar c = (org.exoplatform.calendar.service.Calendar) itr1.next();

            if ((calendarDisplayedMap.get(c.getId()) == null) && !(calendarNonDisplayedMap.containsKey(c.getId()))) {
                calendarNonDisplayedMap.put(c.getId(), c);
                calendarNonDisplayedList.add(c);
            }
        }

        String dateLabel = "";
        if (int_nb_click == 0) dateLabel = "TODAY"+": " ;
        else if (int_nb_click == -1) dateLabel = "YESTERDAY"+": ";
        else if (int_nb_click == 1) dateLabel = "TOMORROW"+": ";
        else dateLabel = "";
        dateLabel = dateLabel +  date_act;

        calendar.with().set("displayedCalendar", calendarDisplayedList).
                set("nonDisplayedCalendar", calendarNonDisplayedList).
                set("calendarDisplayedMap", calendarDisplayedMap).
                set("eventsDisplayedList", eventsDisplayedList).
                set("tasksDisplayedList", tasksDisplayedList).
                set("date_act", dateLabel).render();
    }


    @View
    public void setting() throws Exception {
        setting.with().set("displayedCalendar", calendarDisplayedList).set("nonDisplayedCalendar", calendarNonDisplayedList).render();
    }

    @Action
    public void addCalendar(String calendarId) throws Exception {

        org.exoplatform.calendar.service.Calendar calendar = calendarNonDisplayedMap.get(calendarId);
        if (calendar != null) {
            calendarDisplayedList.add(calendar);
            calendarDisplayedMap.put(calendarId, calendar);
            calendarNonDisplayedList.remove(calendar);
            calendarNonDisplayedMap.remove(calendarId);
        }

        setting.with().set("displayedCalendar", calendarDisplayedList).
                set("nonDisplayedCalendar", calendarNonDisplayedList).render();
    }

    @Action
    public void deleteCalendar(String calendarId) throws Exception {

        org.exoplatform.calendar.service.Calendar calendar = calendarDisplayedMap.get(calendarId);
        if (calendar != null) {
            calendarDisplayedList.remove(calendar);
            calendarNonDisplayedList.add(calendar);
            calendarDisplayedMap.remove(calendarId);
            calendarNonDisplayedMap.put(calendarId, calendar);
        }

        setting.with().set("displayedCalendar", calendarDisplayedList).
                set("nonDisplayedCalendar", calendarNonDisplayedList).set("searchResultList", searchResult).render();
    }


    @Action
    public Response incDate(String nbClick) {
        int int_nb_click = Integer.parseInt(nbclick);
        int_nb_click++;
        nbclick = new Integer(int_nb_click).toString();
        return AgendaPortlet_.index();
    }

    @Action
    public Response decDate(String nbClick) {
        int int_nb_click = Integer.parseInt(nbclick);
        int_nb_click--;
        nbclick = new Integer(int_nb_click).toString();
        return AgendaPortlet_.index();
    }

    @Ajax
    @Resource
    public void getSearchResult(String key) {

        Iterator itr = null;
        if (calendarNonDisplayedList != null) itr = calendarNonDisplayedList.iterator();
        searchResult.clear();
        while (itr.hasNext()) {
            org.exoplatform.calendar.service.Calendar c = (org.exoplatform.calendar.service.Calendar) itr.next();
            if (c.getName().startsWith(key)) searchResult.add(c);
        }
        search.with().set("searchResultList", searchResult).render();
    }


    public static Long incDecJour(Long date, int nbJour) {
        Calendar cal;
        cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.add(Calendar.DAY_OF_MONTH, nbJour);
        return cal.getTime().getTime();
    }


    public String[] getUserGroups(String username) throws Exception {

        Object[] objs = organization_.getGroupHandler().findGroupsOfUser(username).toArray();
        String[] groups = new String[objs.length];
        for (int i = 0; i < objs.length; i++) {
            groups[i] = ((Group) objs[i]).getId();
        }
        return groups;
    }

    public List getAllCal(String username) throws Exception {
        List<org.exoplatform.calendar.service.Calendar> calList = calendarService_.getUserCalendars(username, true);
        List<GroupCalendarData> lgcd = calendarService_.getGroupCalendars(getUserGroups(username), true, username);
        List<String> calIds = new ArrayList<String>();
        for (GroupCalendarData g : lgcd) {
            for (org.exoplatform.calendar.service.Calendar c : g.getCalendars()) {
                if (!calIds.contains(c.getId())) {
                    calIds.add(c.getId());
                    calList.add(c);
                }
            }
        }
        return calList;
    }

    public CalendarSetting getCalendarSetting(String username) throws Exception {
        try {
            return calendarService_.getCalendarSetting(username);
        } catch (Exception e) {
            log.warn("could not get calendar setting of user", e);
            return null;
        }
    }

    String[] getCalendarsIdList(String username) {

        StringBuilder sb = new StringBuilder();
        List<GroupCalendarData> listgroupCalendar = null;
        List<org.exoplatform.calendar.service.Calendar> listUserCalendar = null;
        try {
            listgroupCalendar = calendarService_.getGroupCalendars(getUserGroups(username), true, username);
            listUserCalendar = calendarService_.getUserCalendars(username, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (GroupCalendarData g : listgroupCalendar) {
            for (org.exoplatform.calendar.service.Calendar c : g.getCalendars()) {
                sb.append(c.getId()).append(",");
            }
        }
        for (org.exoplatform.calendar.service.Calendar c : listUserCalendar) {
            sb.append(c.getId()).append(",");
        }
        String[] list = sb.toString().split(",");
        return list;
    }

    List<CalendarEvent> getEvents(String username) {
        String[] calList = getCalendarsIdList(username);

        EventQuery eventQuery = new EventQuery();

        eventQuery.setOrderBy(new String[]{Utils.EXO_FROM_DATE_TIME});

        eventQuery.setCalendarId(calList);
        List<CalendarEvent> userEvents = null;
        try {
            userEvents = calendarService_.getEvents(username, eventQuery, calList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userEvents;
    }
}

