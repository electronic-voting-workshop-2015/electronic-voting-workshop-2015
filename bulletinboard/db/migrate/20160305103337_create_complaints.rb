class CreateComplaints < ActiveRecord::Migration
  def change
      create_table :complaints, id: false do |t|
          t.binary :content
      end
  end
end
