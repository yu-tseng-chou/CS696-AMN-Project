clc;clear;clf;close all;
% Clean up

filelist = dir('images/*.jpg');
m = 4;
n = length(filelist);
for i=1:n
    %% Load the original image
    imname = filelist(i).name;
	im = imread(['images/' imname], 'jpg');
    figure
    subplot(1,2,1);
    title(sprintf('file %s', imname));
    imshow(im);
    
    %% Run FFT on the image
    F = fft2(im2double(im));
    
    % Define parameters required to create bandpass filter M
    height = size(im,1);
    width = size(im,2);
    r1 = height/2 * 0.01;
    r2 = height/2 * 0.04;
    [X,Y] = meshgrid(1:width, 1:height);

%%%%%%%%%%%% Circular bandpass filter %%%%%%%%%%%%
    % Create a logical matrix M
    % True  (1) if (x, y) in between r1 and r2
    % False (0) otherwise
    M = sqrt((X-width/2).^2 + (Y-height/2).^2) > r1 & sqrt((X-width/2).^2 + (Y-height/2).^2) < r2;
   
    % Convert the logical matrix to double matrix
    M = double(M);
%%%%%%%%%%%% Circular bandpass filter %%%%%%%%%%%%


%%%%%%%%%%%% Butterworth bandpass filter %%%%%%%%%%%%
%     p = 8;
%     r = sqrt((X-width/2).^2 + (Y-height/2).^2);
% 
%     M = 1./(1+(r1./r).^(2*p))/2 + ...
%         1./(1+(r./r2).^(2*p))/2;
%%%%%%%%%%%% Butterworth bandpass filter %%%%%%%%%%%%

    % Apply filter M to FFT
    C = M.*fftshift(F);

    %% Run inverse FFT on the image
    I = ifft2(fftshift(C));
    I = im2uint8(I);
%     % Plot filtered image from IFFT
%     figure
%     imshow(I);
%     title(sprintf('r_1 = %.2f r_2 = %.2f', r1, r2));
    
    % Threshold image to filter out unneeded outlines
    threshold = 10;
    I(I >= threshold) = 255;
    I(I < threshold) = 0;
    
    % Plot filtered image from IFFT
    subplot(1,2,2);
    imshow(I);
    title(sprintf('r_1 = %.2f r_2 = %.2f threshold = %d', r1, r2, threshold));
end