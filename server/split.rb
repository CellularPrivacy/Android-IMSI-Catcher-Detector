require 'csv'

CSV.foreach('cell_towers.csv') do |row|
  # radio,mcc,net,area,cell,unit,lon,lat,range,samples,changeable,created,updated,averageSignal
  File.open("data/#{row[1]}.csv", 'a') do |json_writer|
   json_writer.write "#{row[4]},#{row[6]},#{row[7]}\n"
  end
end

