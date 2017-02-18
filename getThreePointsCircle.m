function [x, y, r] = getThreePointsCircle(x1, y1, x2, y2, x3, y3)
    % Get circle origin (x, y) and radius r from three points (x1, y1),
    % (x2, y2), (x3, y3) on the circumference 
    % Reference: http://www.ambrsoft.com/TrigoCalc/Circle3D.htm

    x = ((x1^2 + y1^2)*(y2-y3) + (x2^2 + y2^2)*(y3-y1) + (x3^3 + y3^2)*(y1-y2))/...
        (2*(x1*(y2-y3) - y1*(x2-x3) + x2*y3 - x3*y2));
    y = ((x1^2 + y1^2)*(x3-x2) + (x2^2 + y2^2)*(x1-x3) + (x3^3 + y3^2)*(x2-x1))/...
        (2*(x1*(y2-y3) - y1*(x2-x3) + x2*y3 - x3*y2));
    r = sqrt((x-x1)^2 + (y-y1)^2);
    
end