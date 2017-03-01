function threshold = ComputeThreshold(counts)
% Finds the ideal cutoff point for grayscale => black/white conversion.
% Heuristic: cutoff == midpoint between first two peaks (local max).
%   counts - vector containing grayscale-value counts (should have 2 peaks)
%   threshold - (first_peak_index + second_peak_index)/2

    % find first and second peaks (local maximums)
    first_peak_value = 0;
    first_peak_index = 0;
    second_peak_value = 0;
    second_peak_index = 0;
    for ind = 2:255
        if ((counts(ind) > first_peak_value || counts(ind) > second_peak_value) ...
            && counts(ind-1) < counts(ind) && counts(ind) > counts(ind+1))
            
            second_peak_value = counts(ind);
            second_peak_index = ind;
            if (second_peak_value > first_peak_value)
                temp_index = first_peak_index;
                temp_value = first_peak_value;
                
                first_peak_index = second_peak_index;
                first_peak_value = second_peak_value;
                
                second_peak_index = temp_index;
                second_peak_value = temp_value;
            end
        end
    end
    
    % min_index = (first_peak_index + second_peak_index)/2;
    min_index = 1;
    min_value = max(counts);
    for ind = first_peak_index : sign(second_peak_index-first_peak_index): second_peak_index
        if (counts(ind) < min_value)
            min_index = ind;
            min_value = counts(ind);
        end
    end
    
    threshold = min_index;
end