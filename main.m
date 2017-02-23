clc;clear;clf;close all;
% Clean up

filelist = dir('images/Strip*.jpg');
m = 4;
n = length(filelist);
for i=1:n
    %% Load the original image
    imname = filelist(i).name;
	im = imread(['images/' imname], 'jpg');
   
    
    figure
    subplot(3,1,1);
    imshow(im);
    title(sprintf('Original file %s', imname));
    
    G = rgb2gray(im);
    G = medfilt2(G);
    
    subplot(3,1,2);
    [counts,binLocations] = imhist(G);
    stem(binLocations, counts);
    title('Histogram of gray scale version image');
    
%     % TO DO: get threshold from between peaks
%     first_peak = 0;
%     for ind = 1:256
%         if counts(ind) > first_peak
%             first_peak = counts(ind);
%         end
%     end
    
    threshold = 30; %(first_peak + second_peak))/2;
    T = G;
    T(T > threshold) = 255;
    T(T <= threshold) = 0;
    subplot(3,1,3);
    imshow(T);
    title(sprintf('Filtered image with threshold = %d', threshold));

    
    height = size(T,1);
    rand_y = randperm(height, 3);
    thickness_threshold = 2;
    max_number = 10; % max number of songs
    indexes = zeros(3, max_number);
    
    for n = 1:numel(rand_y)
        y = rand_y(n);
        count = 0;
        thickness = 0;
        index = 1;
        for x = 1:size(T, 2)-1
            
            % reset thickness count if change detected
            if (T(y,x) ~= T(y, x+1))
                % if thickness count exceeds threshold, add the index of change
                % to array
                if thickness > thickness_threshold 
                    count = count + 1;
                    indexes(n, count) = index;                    
                end
                
                index = x;
                %lengths(y, count) = thickness;
                thickness = 0;
            end
            
            
            thickness = thickness + 1;
        end
        
        ind = indexes(n,:);
        g=sprintf('%d, ', ind(ind>0));
        %fprintf('Answer: %s\n', g);
        subplot(3,1,3);
        text(0, height+25*n, sprintf('Detected positions: %s', g));
    end
    

end