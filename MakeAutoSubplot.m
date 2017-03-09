function handle = MakeAutoSubplot(numRows, numCols)
% Creates a new subplot each time "handle()" is called. Subplots are
% arranged in a numRows-by-numCols grid. See Matlab documentation for
% subplot for details.
%
% Return:
%   handle - function pointer; calling it creates the next subplot
%
% Parameters:
%   numRows - number of subplot rows
%   numCols - number of subplot column
    i = 1;
    function g()
        subplot(numRows, numCols, i);
        i = i + 1;
    end
    handle = @g;
end
