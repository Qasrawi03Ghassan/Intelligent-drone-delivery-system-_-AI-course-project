package com.example.demo;

import java.util.List;

public class Cost {

    public static  double distance (City c1,City c2)
    {
        double dx=c1.x-c2.x;
        double dy=c1.y-c2.y;
        return Math.sqrt(dx*dx+ dy*dy);
    }

    public static  double totalCost(List<City> route, double penaltyPerUnsafeCity, boolean closeLoop)
    {
        double sum=0;
        for(int i=0;i<route.size()-1;i++) {
            sum+= distance(route.get(i),route.get(i+1));
        }

        if(closeLoop) // if make the route circular (returning from the last city to the first city)
        {
            sum+=distance(route.get(0),route.get(route.size()-1));
        }
        // calcute the unsafe route
        int unsafe=0;
        for(City c: route)
        {
            if(c.safeToFly==1)
                unsafe++ ;
        }

        return (sum+penaltyPerUnsafeCity*unsafe);

    }
}
